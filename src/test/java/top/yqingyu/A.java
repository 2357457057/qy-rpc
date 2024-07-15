package top.yqingyu;

import top.yqingyu.rpc.annontation.QyRpcProducer;
import top.yqingyu.rpc.producer.Producer;

import java.util.Arrays;

@QyRpcProducer
public class A implements InterfaceA{
    private Producer producer;

    public A(Producer producer) {
        this.producer = producer;
    }

    public static void main(String[] args) throws Exception {
        Producer producer = Producer.Builder.newBuilder()
                .port(4737)
                .build();
        producer.start();
        producer.register(new A(producer));
    }

    public String aaaa(String ...aa) {
        return Arrays.toString(aa) + "说：小苏你妈蛋";
    }

    public void bbbb(String cc) {
        System.out.println(cc + "远程来访");
        throw new RuntimeException("红温模式。。。。。。。。。。。。。。。。。。。。。。。。。");
    }

    @Override
    public void shutdown() throws InterruptedException {
        producer.shutdown();
    }
}
