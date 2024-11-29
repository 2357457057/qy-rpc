package top.yqingyu.rpc.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.cglib.core.ClassLoaderAwareGeneratorStrategy;
import top.yqingyu.common.cglib.core.QyNamingPolicy;
import top.yqingyu.common.cglib.proxy.Enhancer;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.Connection;
import top.yqingyu.qymsg.netty.MsgClient;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.consumer.conf.ProxyMode;
import top.yqingyu.rpc.exception.NoSuchHolderException;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConsumerHolderContext {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHolderContext.class);
    final ConcurrentHashMap<Class<?>, Object> ProxyClassCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConsumerHolder> CONSUMER_MAP = new ConcurrentHashMap<>();
    final RpcLinkId rpcLinkId;
    volatile MethodExecuteInterceptor methodExecuteInterceptor = new MethodExecuteInterceptor() {
    };
    private final ProxyMode proxyMode;

    public ConsumerHolderContext() {
        this.rpcLinkId = new RpcLinkId();
        this.proxyMode = ProxyMode.MIX;
    }

    public ConsumerHolderContext(ProxyMode proxyMode) {
        this.rpcLinkId = new RpcLinkId();
        this.proxyMode = proxyMode;
    }

    public ProxyMode getProxyMode() {
        return proxyMode;
    }

    void addConsumer(Consumer consumer) throws Exception {
        MsgClient client = consumer.getClient();
        Connection connection = client.getConnection();
        String name = consumer.getName();
        QyMsg qyMsg = new QyMsg(MsgType.AC, DataType.OBJECT);
        qyMsg.setFrom(consumer.getId());
        QyMsg back = connection.get(qyMsg, Constants.authenticationWaitTime);
        String tag = MsgHelper.gainMsgValue(back, Constants.serviceIdentifierTag);
        logger.info("created rpc connection：{} ,server tag {}", name, tag);
        client.returnConnection(connection);
        if (!CONSUMER_MAP.containsKey(name)) {
            ConsumerHolder holder = new ConsumerHolder(tag, this);
            holder.add(consumer, tag);
            CONSUMER_MAP.put(name, holder);
            return;
        }
        ConsumerHolder holder = CONSUMER_MAP.get(name);
        holder.add(consumer, tag);
    }

    public ConsumerHolder getConsumerHolder(String consumerName) {
        ConsumerHolder consumerHolder = CONSUMER_MAP.get(consumerName);
        if (consumerHolder == null)
            throw new NoSuchHolderException("can not find holder name is: {}", consumerName);
        return consumerHolder;
    }

    public <T> T getProxy(String consumerName, Class<T> clazz) {
        if (ProxyClassCache.containsKey(clazz)) {
            return (T) ProxyClassCache.get(clazz);
        }
        return switch (proxyMode) {
            case JDK -> getJDKProxy(consumerName, clazz);
            case CGlib -> getCGlibProxy(consumerName, clazz);
            case MIX -> clazz.isInterface() ? getJDKProxy(consumerName, clazz) : getCGlibProxy(consumerName, clazz);
        };
    }

    private <T> T getCGlibProxy(String consumerName, Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setNamingPolicy(QyNamingPolicy.INSTANCE);
        enhancer.setCallback(new CGLibMethodExecProxy(clazz, consumerName, this));
        enhancer.setAttemptLoad(true);
        ClassLoader classLoader = RpcUtil.getClassLoader(clazz);
        if (classLoader != null)
            enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));
        T t = (T) enhancer.create();
        ProxyClassCache.put(clazz, t);
        return t;
    }

    private <T> T getJDKProxy(String consumerName, Class<T> clazz) {
        if (!clazz.isInterface()) {
            logger.warn("JDK Proxy only support interface ,will use CGLIB");
            return getCGlibProxy(consumerName, clazz);
        }
        JDKMethodExecProxy proxy = new JDKMethodExecProxy(clazz, consumerName, this);
        T t = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, proxy);
        ProxyClassCache.put(clazz, t);
        return t;
    }

    public void shutdown() {
        CONSUMER_MAP.forEach((s, holder) -> {
            List<Consumer> consumerList = holder.consumerList;
            for (Consumer consumer : consumerList) {
                try {
                    consumer.shutdown();
                } catch (InterruptedException e) {
                    logger.error("qyrpc consumer {} shutdown error", s, e);
                }
            }
            logger.info("qyrpc consumer {} is shutdown", s);
        });
    }

    public void setLinkId(String id) {
        rpcLinkId.setLinkId(id);
    }

    public void setLinkId(String th, String id) {
        rpcLinkId.setLinkId(th, id);
    }

    public void removeLinkId(String th) {
        rpcLinkId.removeLinkId(th);
    }

    public void removeLinkId() {
        rpcLinkId.removeLinkId(Thread.currentThread().getName());
    }

    public void setMethodExecuteInterceptor(MethodExecuteInterceptor methodExecuteInterceptor) {
        this.methodExecuteInterceptor = methodExecuteInterceptor;
    }
}
