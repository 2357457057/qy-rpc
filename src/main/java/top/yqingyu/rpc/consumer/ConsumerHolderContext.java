package top.yqingyu.rpc.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.Connection;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.exception.NoSuchHolderException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ConsumerHolderContext {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerHolderContext.class);
    private final ConcurrentHashMap<String, ConsumerHolder> CONSUMER_MAP = new ConcurrentHashMap<>();
    final RpcLinkId rpcLinkId;
    MethodExecuteInterceptor methodExecuteInterceptor = new MethodExecuteInterceptor() {
    };

    public ConsumerHolderContext() {
        this.rpcLinkId = new RpcLinkId();
    }

    public ConsumerHolderContext(MethodExecuteInterceptor methodExecuteInterceptor) {
        this.methodExecuteInterceptor = methodExecuteInterceptor;
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

    public <T> T getProxy(String consumerName, Class<T> clazz) {
        return getConsumerHolder(consumerName).getProxy(clazz);
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

    public void setLinkId(Thread th, String id) {
        rpcLinkId.setLinkId(th, id);
    }
}
