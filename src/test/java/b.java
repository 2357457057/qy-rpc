import top.yqingyu.A;
import top.yqingyu.qymsg.netty.ConnectionConfig;
import top.yqingyu.rpc.consumer.Consumer;
import top.yqingyu.rpc.consumer.HolderCache;

public class b {
    public static void main(String[] args) throws Throwable {
        HolderCache holderCache = new HolderCache();
        Consumer consumer = Consumer.create(new ConnectionConfig.Builder().build(), holderCache);
        A proxy = holderCache.getProxy(consumer.getName(), A.class);
        System.out.println(proxy.toString());
        System.out.println(proxy.aaaa("轻语"));
        System.out.println(proxy.aaaa("轻语"));
        proxy.bbbb("小苏");
    }
}
