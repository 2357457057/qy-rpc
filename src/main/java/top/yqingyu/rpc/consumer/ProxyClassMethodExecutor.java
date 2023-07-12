package top.yqingyu.rpc.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.cglib.proxy.MethodInterceptor;
import top.yqingyu.common.cglib.proxy.MethodProxy;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.Connection;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.annontation.QyRpcProducerProperties;
import top.yqingyu.rpc.exception.RpcException;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyClassMethodExecutor implements MethodInterceptor {

    public static final Logger logger = LoggerFactory.getLogger(ProxyClassMethodExecutor.class);
    Class<?> proxyClass;
    ConsumerHolder holder;
    ConcurrentHashMap<Method, QyRpcProducerProperties> methodPropertiesCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Method, Boolean> emptyMethodPropertiesCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Method, String> methodNameCache = new ConcurrentHashMap<>();
    ConcurrentHashMap<Method, Object> specialMethodNameCache = new ConcurrentHashMap<>();
    final static Boolean b = false;

    public ProxyClassMethodExecutor(Class<?> proxyClass, ConsumerHolder holder) {
        this.proxyClass = proxyClass;
        this.holder = holder;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] param, MethodProxy proxy) throws Throwable {
        Object result = specialMethodNameCache.get(method);
        if (result != null) {
            return result;
        }
        String string = methodNameCache.get(method);
        if (StringUtil.isEmpty(string)) {
            String className = RpcUtil.getClassName(proxyClass);
            String name = method.getName();
            StringBuilder sb = new StringBuilder(className).append(Constants.method).append(name);
            if (param != null) for (Object o : param) {
                sb.append("#").append(o.getClass().getName());
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
                if (consumer == null || retryDiff)
                    consumer = holder.next();
                qyMsg.putMsg(string);
                if (consumer == null) continue;
                qyMsg.setFrom(consumer.getId());
                QyMsg back = get(wait, consumer, qyMsg, waitTime);
                String type = MsgHelper.gainMsg(back);
                switch (type) {
                    case Constants.invokeSuccess -> {
                        return back.getDataMap().get(Constants.invokeResult);
                    }
                    case Constants.invokeThrowError -> {
                        Throwable error = new Throwable("remote process error", (Throwable) back.getDataMap().get(Constants.invokeResult));
                        if (retryTimes - 1 == i)
                            throw error;
                        logger.error("", error);
                    }
                    case Constants.invokeNoSuch -> {
                        return invokeNoSuch(obj, method, param);
                    }
                }
            }
            return null;
        }
        Consumer consumer = holder.next();
        qyMsg.putMsg(string);
        if (consumer != null) {
            qyMsg.setFrom(consumer.getId());
            QyMsg back = get(wait, consumer, qyMsg, waitTime);
            String type = MsgHelper.gainMsg(back);
            switch (type) {
                case Constants.invokeSuccess -> {
                    return back.getDataMap().get(Constants.invokeResult);
                }
                case Constants.invokeNoSuch -> {
                    return invokeNoSuch(obj, method, param);
                }
                case Constants.invokeThrowError ->
                        throw new Throwable("remote process error", (Throwable) back.getDataMap().get(Constants.invokeResult));
                default -> throw new RpcException("unknown type {}", type);
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
        if (trn != null)
            specialMethodNameCache.put(method, trn);
        return trn;
    }

    private static boolean check(long l) {
        return l > 0;
    }

    private static QyMsg get(boolean b, Consumer consumer, QyMsg in, long time) throws Exception {
        Connection connection = consumer.getClient().getConnection();
        return b ? connection.get(in, time) : connection.get(in);
    }

    String proxyToString() {
        return proxyClass.getSimpleName() + "@" + proxyHashCode();
    }

    int proxyHashCode() {
        return this.hashCode();
    }

    boolean proxyEquals(Object obj, Object[] param) {
        if (param == null)
            return false;
        return obj.equals(param[0]);
    }

}