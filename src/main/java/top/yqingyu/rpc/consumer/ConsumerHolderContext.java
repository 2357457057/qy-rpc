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
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.exception.NoSuchHolderException;
import top.yqingyu.rpc.util.RpcUtil;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConsumerHolderContext {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHolderContext.class);
    final ConcurrentHashMap<Class<?>, Object> ProxyClassCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConsumerHolder> CONSUMER_MAP = new ConcurrentHashMap<>();
    final RpcLinkId rpcLinkId;
    volatile MethodExecuteInterceptor methodExecuteInterceptor = new MethodExecuteInterceptor() {
    };

    public ConsumerHolderContext() {
        this.rpcLinkId = new RpcLinkId();
    }

    void addConsumer(Consumer consumer) throws Exception {
        Connection connection = consumer.getClient().getConnection();
        String name = consumer.getName();
        QyMsg qyMsg = new QyMsg(MsgType.AC, DataType.OBJECT);
        QyMsg back = connection.get(qyMsg, Constants.authenticationWaitTime);
        String s = MsgHelper.gainMsgValue(back, Constants.serviceIdentifierTag);
        if (!CONSUMER_MAP.containsKey(name)) {
            ConsumerHolder holder = new ConsumerHolder(s, this);
            holder.add(consumer, s);
            CONSUMER_MAP.put(name, holder);
            return;
        }
        ConsumerHolder holder = CONSUMER_MAP.get(name);
        holder.add(consumer, s);
    }

    public ConsumerHolder getConsumerHolder(String consumerName) {
        ConsumerHolder consumerHolder = CONSUMER_MAP.get(consumerName);
        if (consumerHolder == null)
            throw new NoSuchHolderException("未配置名为{}的holder", consumerName);
        return consumerHolder;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(String consumerName, Class<T> clazz) {
        if (ProxyClassCache.containsKey(clazz)) {
            return (T) ProxyClassCache.get(clazz);
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setNamingPolicy(QyNamingPolicy.INSTANCE);
        enhancer.setCallback(new ProxyClassMethodExecutor(clazz, consumerName, this));
        enhancer.setAttemptLoad(true);
        ClassLoader classLoader = RpcUtil.getClassLoader(clazz);
        if (classLoader != null)
            enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));
        T t = (T) enhancer.create();
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

    public void removeLinkId(String th){
        rpcLinkId.removeLinkId(th);
    }
    public void removeLinkId(){
        rpcLinkId.removeLinkId(Thread.currentThread().getName());
    }

    public void setMethodExecuteInterceptor(MethodExecuteInterceptor methodExecuteInterceptor) {
        this.methodExecuteInterceptor = methodExecuteInterceptor;
    }
}
