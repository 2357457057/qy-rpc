package top.yqingyu.rpc.consumer;


import top.yqingyu.qymsg.netty.ConnectionConfig;
import top.yqingyu.qymsg.netty.MsgClient;


public class Consumer {
    MsgClient client;

    String name = "    ".repeat(8);

    Consumer() {
    }

    public static Consumer create(ConnectionConfig config) throws Exception {
        Consumer consumer = new Consumer();
        consumer.client = MsgClient.create(config);
        TransRpc.addConsumer(consumer);
        return consumer;
    }

    public MsgClient getClient() {
        return client;
    }

    public void setName(String name) {
        char[] charArray = this.name.toCharArray();
        char[] charArray2 = name.toCharArray();
        int min = Math.min(charArray.length, charArray2.length);
        System.arraycopy(charArray2, 0, charArray, 0, min);
        this.name = new String(charArray);
    }

    public String getName() {
        return name;
    }
}
