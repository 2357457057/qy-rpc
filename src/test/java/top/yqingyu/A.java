package top.yqingyu;

import top.yqingyu.rpc.annontation.QyRpcProducer;
import top.yqingyu.rpc.producer.Producer;

import java.util.Arrays;

@QyRpcProducer
public class A implements InterfaceA{
    public static void main(String[] args) throws Exception {
        Producer producer = Producer.Builder.newBuilder()
                .port(4737)
                .build();
        producer.start();
        producer.register(new A());
        Thread.sleep(9000000);
    }

    public String aaaa(String ...aa) {
        System.out.println("你好呀" + Arrays.toString(aa));
        return Arrays.toString(aa) + "说：小苏你妈蛋";
    }

    public void bbbb(String cc) {
        System.out.println(cc + "远程来访");
        throw new RuntimeException("红温模式。。。。。。。。。。。。。。。。。。。。。。。。。");
    }
}
