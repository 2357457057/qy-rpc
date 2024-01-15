import lombok.extern.slf4j.Slf4j;
import top.yqingyu.qymsg.netty.ConnectionConfig;
import top.yqingyu.qyws.modules.web.service.ViewNumService;
import top.yqingyu.rpc.consumer.Consumer;
import top.yqingyu.rpc.consumer.ConsumerHolderContext;

@Slf4j
public class b {

    public static void main(String[] args) throws Throwable {
        ConsumerHolderContext consumerHolderContext = new ConsumerHolderContext();
        ConnectionConfig build = new ConnectionConfig.Builder()
                .host("192.168.50.68")
                .poolMin(10)
                .poolMax(10)
                .port(4737)
                .build();
        Consumer consumer = Consumer.create(build, consumerHolderContext);
//        A proxy = consumerHolderContext.getProxy(consumer.getName(), A.class);
//        proxy.bbbb("轻语");
        remoteHandle(consumerHolderContext, consumer);
    }

    public static void remoteHandle(ConsumerHolderContext consumerHolderContext, Consumer consumer) {
        ViewNumService proxy = consumerHolderContext.getProxy(consumer.getName(), ViewNumService.class);
        log.info(proxy.toString());

        consumerHolderContext.setLinkId("session5");
        log.info(proxy.getViewNum());
        consumerHolderContext.setLinkId("session2");
        log.info(proxy.getViewNum());
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    log.info(proxy.getViewNum());
                    log.info("{}", proxy.getIpInfo("96.201.45.89"));
                } catch (Exception e) {
                    log.error("", e);
                }
            }).start();
        }
    }
}
