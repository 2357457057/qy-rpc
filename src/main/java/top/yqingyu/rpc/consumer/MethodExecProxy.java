package top.yqingyu.rpc.consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.qydata.DataMap;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.qymsg.*;
import top.yqingyu.qymsg.netty.Connection;
import top.yqingyu.qymsg.netty.MsgClient;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.annontation.QyRpcProducerProperties;
import top.yqingyu.rpc.exception.RemoteServerException;
import top.yqingyu.rpc.exception.RpcException;
import top.yqingyu.rpc.exception.RpcTimeOutException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class MethodExecProxy {
    protected final Logger logger = LoggerFactory.getLogger(MethodExecProxy.class);
    ConsumerHolder holder;
    protected final ProxyMetaDataCache cache;
    protected final Class<?> proxyClass;
    protected final ConsumerHolderContext ctx;
    protected final String holderName;
    protected final MethodExecuteInterceptor interceptor;

    public MethodExecProxy(Class<?> proxyClass, String consumerName, ConsumerHolderContext ctx) {
        this.proxyClass = proxyClass;
        this.ctx = ctx;
        this.holderName = consumerName;
        this.interceptor = ctx.methodExecuteInterceptor;
        cache = new ProxyMetaDataCache(proxyClass);
    }

    public Object invoke0(Object obj, Method method, Object[] param) throws Throwable {
        if (holder == null) {
            holder = ctx.getConsumerHolder(holderName);
            if (holder == null) throw new RpcException("No consumer named {} was initialized please check", holderName);
        }
        interceptor.before(ctx, method, param);
        Object result = cache.getSpecialMethodName(method);
        if (result != null) {
            interceptor.completely(ctx, method, param, result);
            return result;
        }
        String methodStrName = cache.getMethodName(method, param);

        boolean retry = false;
        boolean wait = false;
        boolean retryDiff = false;
        int retryTimes = 0;
        long waitTime = 0;

        QyRpcProducerProperties annotation = cache.getMethodProperties(method);
        if (annotation != null) {
            retryTimes = annotation.retryTimes();
            waitTime = annotation.waitTime();
            retry = check(retryTimes);
            wait = check(waitTime);
            retryDiff = annotation.retryDiffProducer();
        }

        QyMsg qyMsg = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
        qyMsg.putMsgData(Constants.parameterList, param);
        String linkId = ctx.rpcLinkId.getLinkId();
        qyMsg.putMsgData(Constants.linkId, StringUtil.isEmpty(linkId) ? "RM-" + Thread.currentThread().getName() : linkId);
        if (retry) {
            return handleRetryMode(obj, method, param, retryTimes, retryDiff, qyMsg, methodStrName, wait, waitTime);
        }
        return handleOnceMode(obj, method, param, qyMsg, methodStrName, wait, waitTime);
    }


    @Nullable
    private Object handleOnceMode(Object obj, Method method, Object[] param, QyMsg qyMsg, String methodStrName, boolean wait, long waitTime) throws Throwable {
        Consumer consumer = holder.next();
        if (consumer == null) {
            logger.warn("cannot gen can used consumer");
            return null;
        }

        qyMsg.putMsg(methodStrName);
        Object result = null;
        qyMsg.setFrom(consumer.getId());
        logger.debug("invoke: {}", methodStrName);
        QyMsg back = get(wait, consumer, qyMsg, waitTime);
        remoteProcessError(back, methodStrName, e -> interceptor.error(ctx, method, param, e));
        String type = MsgHelper.gainMsg(back);
        switch (type) {
            case Constants.invokeSuccess -> {
                result = back.getDataMap().get(Constants.invokeResult);
                logger.debug("invokeSuccess : {}", methodStrName);
                interceptor.completely(ctx, method, param, result);
                return result;
            }
            case Constants.invokeNoSuch -> {
                logger.debug("invokeNoSuch: {}", methodStrName);
                interceptor.completely(ctx, method, param, result);
                return invokeNoSuch(obj, method, param);
            }
            case Constants.invokeThrowError -> {
                Throwable error = handleRemoteException(method, methodStrName, back);
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

    @Nullable
    private Object handleRetryMode(Object obj, Method method, Object[] param, int retryTimes, boolean retryDiff, QyMsg qyMsg, String methodStrName, boolean wait, long waitTime) throws Throwable {
        Object result = null;
        Consumer consumer = null;
        for (int i = 0; i < retryTimes; i++) {
            if (consumer == null || retryDiff) consumer = holder.next();
            if (consumer == null) {
                logger.warn("cannot gen can used consumer");
                continue;
            }
            qyMsg.putMsg(methodStrName);
            qyMsg.setFrom(consumer.getId());
            logger.debug("send invoke: {}", methodStrName);
            QyMsg back = get(wait, consumer, qyMsg, waitTime);
            remoteProcessError(back, methodStrName, e -> interceptor.error(ctx, method, param, e));
            String type = MsgHelper.gainMsg(back);
            switch (type) {
                case Constants.invokeSuccess -> {
                    logger.debug("invokeSuccess: {}", methodStrName);
                    result = back.getDataMap().get(Constants.invokeResult);
                    interceptor.completely(ctx, method, param, result);
                    return result;
                }
                case Constants.invokeThrowError -> {
                    Throwable error = handleRemoteException(method, methodStrName, back);
                    interceptor.error(ctx, method, param, error);
                    if (retryTimes - 1 == i) {
                        throw error;
                    }
                    logger.error("", error);
                }
                case Constants.invokeNoSuch -> {
                    logger.debug("invokeNoSuch: {}", methodStrName);
                    interceptor.completely(ctx, method, param, result);
                    return invokeNoSuch(obj, method, param);
                }
            }
        }
        interceptor.completely(ctx, method, param, result);
        return null;
    }

    private Throwable handleRemoteException(Method method, String methodStrName, QyMsg back) {
        logger.debug("invokeThrowError: {}", methodStrName);
        DataMap bodyData = back.getDataMap();
        String errorClass = bodyData.getString(Constants.invokeErrorClass);
        String errorMessage = bodyData.getString(Constants.invokeErrorMessage);
        logger.error("Error from remote server {},>>>{}: {}\n{}", holderName, errorClass, errorMessage, bodyData.getString(Constants.invokeResult));
        if (StringUtil.isNotEmpty(errorClass)) {
            try {
                Class<?> aClass = Class.forName(errorClass);
                try {
                    Constructor<?> declaredConstructor = aClass.getDeclaredConstructor(String.class);
                    declaredConstructor.setAccessible(true);
                    return (Throwable) declaredConstructor.newInstance(StringUtil.fillBrace("Anomaly simulation for remote server {},>>>{}: {}", holderName, errorClass, errorMessage));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException ignored) {
                    Constructor<?> declaredConstructor = aClass.getDeclaredConstructor();
                    declaredConstructor.setAccessible(true);
                    return (Throwable) declaredConstructor.newInstance();
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ignored) {
            }
        }
        return new RemoteServerException("remote server {} process error and can't simulate remote server exception", holderName);
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
        cache.putSpecialMethodNameCache(method, trn);
        return trn;
    }

    private static boolean check(long l) {
        return l > 0;
    }

    private static QyMsg get(boolean b, Consumer consumer, QyMsg in, long time) throws Exception {
        QyMsg qyMsg;
        MsgClient client = consumer.getClient();
        Connection connection = null;
        try {
            connection = client.getConnection();
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
        } finally {
            client.returnConnection(connection);
        }
        return qyMsg;
    }

    String proxyToString() {
        return proxyClass.getSimpleName() + "@" + proxyHashCode();
    }

    void remoteProcessError(QyMsg back, String invokeTarget, java.util.function.Consumer<RpcException> consumer) {
        String gainMsg = MsgHelper.gainMsg(back);
        if (MsgType.ERR_MSG.equals(back.getMsgType())) {
            Object o = back.getDataMap().get(Dict.ERR_MSG_EXCEPTION);
            if (o != null) {
                RpcException rpcException = new RpcException((Exception) o, "remote process error, {} , invokeTarget:{}", gainMsg, invokeTarget);
                consumer.accept(rpcException);
                throw rpcException;
            }
            RpcException rpcException = new RpcException("remote process error, {} , invokeTarget:{}", gainMsg, invokeTarget);
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
