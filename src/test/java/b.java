import top.yqingyu.A;
import top.yqingyu.qymsg.netty.ConnectionConfig;
import top.yqingyu.rpc.consumer.TransRpc;
import top.yqingyu.rpc.consumer.Consumer;

public class b {
    public static void main(String[] args) throws Throwable {
        Consumer.create(new ConnectionConfig.Builder().build());
        TransRpc transRpc = new TransRpc();
        A proxy = transRpc.getProxy(A.class);
        System.out.println(proxy.aaaa("轻语"));
        System.out.println(proxy.aaaa("轻语"));
        proxy.bbbb("小苏");
    }
}
