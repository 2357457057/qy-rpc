package top.yqingyu.rpc.consumer;


import top.yqingyu.common.utils.UUIDUtil;
import top.yqingyu.qymsg.socket.ConnectionConfig;
import top.yqingyu.qymsg.socket.MsgClient;


public class Consumer {
    MsgClient client;

    String name;
    /**
     * 必须为长度32的字串。
     */
    String id = UUIDUtil.randomUUID().toString2();

    Consumer() {
    }

    public static Consumer create(ConnectionConfig config, ConsumerHolderContext consumerHolderContext) throws Exception {
        Consumer consumer = new Consumer();
        consumer.client = MsgClient.create(config);
        consumer.name = config.getName();
        consumerHolderContext.addConsumer(consumer);
        return consumer;
    }

    public void shutdown() throws InterruptedException {
        client.shutdown();
    }

    public MsgClient getClient() {
        return client;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
