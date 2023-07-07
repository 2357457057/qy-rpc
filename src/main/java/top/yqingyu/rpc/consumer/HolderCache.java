package top.yqingyu.rpc.consumer;

import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.Connection;
import top.yqingyu.rpc.Dict;
import top.yqingyu.rpc.exception.NoSuchHolderException;

import java.util.concurrent.ConcurrentHashMap;


public class HolderCache {

    public final ConcurrentHashMap<String, ConsumerHolder> CONSUMER_MAP = new ConcurrentHashMap<>();

    void addConsumer(Consumer consumer) throws Exception {
        Connection connection = consumer.getClient().getConnection();
        String name = consumer.getName();
        QyMsg qyMsg = new QyMsg(MsgType.AC, DataType.OBJECT);
        QyMsg back = connection.get(qyMsg, Dict.authenticationWaitTime);
        String s = MsgHelper.gainMsgValue(back, Dict.serviceIdentifierTag);
        if (!CONSUMER_MAP.containsKey(name)) {
            ConsumerHolder holder = new ConsumerHolder(s);
            holder.add(consumer, s);
            CONSUMER_MAP.put(name, holder);
            return;
        }
        ConsumerHolder holder = CONSUMER_MAP.get(s);
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
}
