package top.yqingyu;

import top.yqingyu.rpc.annontation.TransRpc;
import top.yqingyu.rpc.producer.Producer;

@TransRpc
public class A {
    public static void main(String[] args) throws Exception {
        Producer producer = Producer.create(32, 4729);
        producer.register(new A());
        Thread.sleep(9000000);
    }

    public String aaaa(String aa) {
        System.out.println("你好呀" + aa);
        return aa + "说：小苏你妈蛋";    }

    public void bbbb(String cc) {
        System.out.println(cc + "远程来访");
        throw new RuntimeException("红温模式。。。。。。。。。。。。。。。。。。。。。。。。。");
    }
}
