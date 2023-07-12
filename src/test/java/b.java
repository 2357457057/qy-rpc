import top.yqingyu.A;
import top.yqingyu.qymsg.netty.ConnectionConfig;
import top.yqingyu.rpc.consumer.Consumer;
import top.yqingyu.rpc.consumer.ConsumerHolderContext;

public class b {
    public static void main(String[] args) throws Throwable {
        ConsumerHolderContext consumerHolderContext = new ConsumerHolderContext();
        Consumer consumer = Consumer.create(new ConnectionConfig.Builder().build(), consumerHolderContext);
        A proxy = consumerHolderContext.getProxy(consumer.getName(), A.class);
        System.out.println(proxy.toString());
        System.out.println(proxy.aaaa("轻语"));
        System.out.println(proxy.aaaa("轻语"));
        proxy.bbbb("小苏");
    }
}
