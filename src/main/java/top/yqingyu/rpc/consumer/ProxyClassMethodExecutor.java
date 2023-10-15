package top.yqingyu.rpc.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.cglib.proxy.MethodInterceptor;
import top.yqingyu.common.cglib.proxy.MethodProxy;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.qymsg.*;
import top.yqingyu.qymsg.socket.Connection;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.annontation.QyRpcProducerProperties;
import top.yqingyu.rpc.exception.RpcException;
import top.yqingyu.rpc.exception.RpcTimeOutException;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyClassMethodExecutor implements MethodInterceptor {

    public static final Logger logger = LoggerFactory.getLogger(ProxyClassMethodExecutor.class);
    Class<?> proxyClass;
    String holderName;
    ConsumerHolder holder;
    ConsumerHolderContext ctx;
    MethodExecuteInterceptor interceptor;
    ConcurrentHashMap<Method, QyRpcProducerProperties> methodPropertiesCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Method, Boolean> emptyMethodPropertiesCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Method, String> methodNameCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Method, Object> specialMethodNameCache = new ConcurrentHashMap<>();
    final static Boolean b = false;

    public ProxyClassMethodExecutor(Class<?> proxyClass, String consumerName, ConsumerHolderContext ctx) {
        this.proxyClass = proxyClass;
        this.ctx = ctx;
        holderName = consumerName;
        interceptor = ctx.methodExecuteInterceptor;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] param, MethodProxy proxy) throws Throwable {
        if (holder == null) {
            holder = ctx.getConsumerHolder(holderName);
            if (holder == null) throw new RpcException("No consumer named {} was initialized please check", holderName);
        }
        interceptor.before(ctx, method, param);
        Object result = specialMethodNameCache.get(method);
        if (result != null) {
            interceptor.completely(ctx, method, param, result);
            return result;
        }
        String string = methodNameCache.get(method);
        if (StringUtil.isEmpty(string)) {
            String className = RpcUtil.getClassName(proxyClass);
            String name = method.getName();

            StringBuilder sb = new StringBuilder(className).append(Constants.method).append(name);
            if (param != null) for (Class<?> o : method.getParameterTypes()) {
                sb.append("#").append(o.getName());
            }
            string = sb.toString();
            methodNameCache.put(method, string);
        }

        boolean retry = false;
        boolean wait = false;
        boolean retryDiff = false;
        int retryTimes = 0;
        long waitTime = 0;
        if (!emptyMethodPropertiesCache.containsKey(method)) {
            QyRpcProducerProperties annotation = methodPropertiesCache.get(method);
            if (annotation == null) {
                annotation = method.getAnnotation(QyRpcProducerProperties.class);
            }
            if (annotation == null) {
                annotation = proxyClass.getAnnotation(QyRpcProducerProperties.class);
            }
            if (annotation != null) {
                retryTimes = annotation.retryTimes();
                waitTime = annotation.waitTime();
                retry = check(retryTimes);
                wait = check(waitTime);
                retryDiff = annotation.retryDiffProducer();
            } else {
                emptyMethodPropertiesCache.put(method, b);
            }
        }

        QyMsg qyMsg = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
        qyMsg.putMsgData(Constants.parameterList, param);
        qyMsg.putMsgData(Constants.linkId, holder.ctx.rpcLinkId.getLinkId());
        if (retry) {
            Consumer consumer = null;
            for (int i = 0; i < retryTimes; i++) {
                if (consumer == null || retryDiff) consumer = holder.next();
                qyMsg.putMsg(string);
                if (consumer == null) continue;
                qyMsg.setFrom(consumer.getId());
                logger.debug("send invoke: {}", string);
                QyMsg back = get(wait, consumer, qyMsg, waitTime);
                remoteProcessError(back, string, e -> interceptor.error(ctx, method, param, e));
                String type = MsgHelper.gainMsg(back);
                switch (type) {
                    case Constants.invokeSuccess -> {
                        logger.debug("invokeSuccess: {}", string);
                        result = back.getDataMap().get(Constants.invokeResult);
                        interceptor.completely(ctx, method, param, result);
                        return result;
                    }
                    case Constants.invokeThrowError -> {
                        logger.debug("invokeThrowError: {}", string);
                        Throwable error = new Throwable("remote process error", (Throwable) back.getDataMap().get(Constants.invokeResult));
                        interceptor.error(ctx, method, param, error);
                        if (retryTimes - 1 == i) {
                            throw error;
                        }
                        logger.error("", error);
                    }
                    case Constants.invokeNoSuch -> {
                        logger.debug("invokeNoSuch: {}", string);
                        interceptor.completely(ctx, method, param, result);
                        return invokeNoSuch(obj, method, param);
                    }
                }
            }
            interceptor.completely(ctx, method, param, result);
            return null;
        }
        Consumer consumer = holder.next();
        qyMsg.putMsg(string);
        if (consumer != null) {
            qyMsg.setFrom(consumer.getId());
            logger.debug("invoke: {}", string);
            QyMsg back = get(wait, consumer, qyMsg, waitTime);
            remoteProcessError(back, string, e -> interceptor.error(ctx, method, param, e));
            String type = MsgHelper.gainMsg(back);
            switch (type) {
                case Constants.invokeSuccess -> {
                    result = back.getDataMap().get(Constants.invokeResult);
                    logger.debug("invokeSuccess : {}", string);
                    interceptor.completely(ctx, method, param, result);
                    return result;
                }
                case Constants.invokeNoSuch -> {
                    logger.debug("invokeNoSuch: {}", string);
                    interceptor.completely(ctx, method, param, result);
                    return invokeNoSuch(obj, method, param);
                }
                case Constants.invokeThrowError -> {
                    Throwable error = new Throwable("remote process error", (Throwable) back.getDataMap().get(Constants.invokeResult));
                    logger.debug("invokeThrowError: {}", string);
                    interceptor.error(ctx, method, param, error);
                    throw error;
                }
                default -> {
                    RpcException rpcException = new RpcException("unknown type {}", type);
                    interceptor.error(ctx, method, param, rpcException);
                    throw rpcException;
                }
            }
        }
        return null;
    }

    private Object invokeNoSuch(Object obj, Method method, Object[] param) {
        String methodName = method.getName();
        if (!Constants.specialMethod.contains(methodName)) {
            return null;
        }
        Object trn = null;
        if ("toString".equals(methodName)) {
            trn = proxyToString();
        }
        if ("hashcode".equals(methodName)) {
            trn = proxyHashCode();
        }
        if ("equals".equals(methodName)) {
            trn = proxyEquals(obj, param);
        }
        if (trn != null) specialMethodNameCache.put(method, trn);
        return trn;
    }

    private static boolean check(long l) {
        return l > 0;
    }

    private static QyMsg get(boolean b, Consumer consumer, QyMsg in, long time) throws Exception {
        Connection connection = consumer.getClient().getConnection();
        QyMsg qyMsg;
        if (b) {
            qyMsg = connection.get(in, time);
            if (qyMsg == null) {
                throw new RpcTimeOutException("rpc invoke time out time {}", time);
            }
            return qyMsg;
        }
        qyMsg = connection.get(in);
        if (qyMsg == null) {
            throw new RpcException("can not receive back msg");
        }
        return qyMsg;
    }

    String proxyToString() {
        return proxyClass.getSimpleName() + "@" + proxyHashCode();
    }

    void remoteProcessError(QyMsg back, String invokeTarget, java.util.function.Consumer<RpcException> consumer) {
        if (MsgType.ERR_MSG.equals(back.getMsgType())) {
            Object o = back.getDataMap().get(Dict.ERR_MSG_EXCEPTION);
            if (o != null) {
                RpcException rpcException = new RpcException((Exception) o, "remote process error, {} , invokeTarget:{}", Dict.ERR_MSG_EXCEPTION, invokeTarget);
                consumer.accept(rpcException);
                throw rpcException;
            }
            RpcException rpcException = new RpcException("remote process error, invokeTarget:{}", invokeTarget);
            consumer.accept(rpcException);
            throw rpcException;
        }
    }

    int proxyHashCode() {
        return this.hashCode();
    }

    boolean proxyEquals(Object obj, Object[] param) {
        if (param == null) return false;
        return obj.equals(param[0]);
    }

}