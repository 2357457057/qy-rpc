package top.yqingyu.rpc.consumer;


import top.yqingyu.qymsg.netty.MsgClient;
import top.yqingyu.rpc.consumer.conf.ConsumerConfig;


public class Consumer {
    MsgClient client;

    ConsumerConfig config;

    /**
     * 必须为长度32的字串。
     */

    Consumer(ConsumerConfig config) {
        this.config = config;
        this.client = MsgClient.create(config.getConnectionConfig());
    }

    public static Consumer create(ConsumerConfig config, ConsumerHolderContext consumerHolderContext) throws Exception {
        Consumer consumer = new Consumer(config);
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
        return config.name;
    }

    public void setName(String name) {
        config.name = name;
    }

    public String getId() {
        return config.id;
    }

    public void setId(String id) {
        config.id = id;
    }
}
