import lombok.extern.slf4j.Slf4j;
import top.yqingyu.InterfaceA;
import top.yqingyu.common.utils.LocalDateTimeUtil;
import top.yqingyu.qyws.modules.web.service.ViewNumService;
import top.yqingyu.rpc.consumer.Consumer;
import top.yqingyu.rpc.consumer.ConsumerHolderContext;
import top.yqingyu.rpc.consumer.conf.ConsumerConfig;

import java.time.LocalDateTime;

@Slf4j
public class b {

    public static void main(String[] args) throws Throwable {
        ConsumerHolderContext consumerHolderContext = new ConsumerHolderContext();
        ConsumerConfig config = new ConsumerConfig();
        config.setHost("127.0.0.1");
        config.setPoolMin(10);
        config.setPoolMax(10);
        config.setPort(4737);
        Consumer.create(config, consumerHolderContext);
        InterfaceA proxy = consumerHolderContext.getProxy(config.getName(), InterfaceA.class);
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10000; i++) {
            proxy.aaaa("轻语", "yyj");
        }
        System.out.println(LocalDateTimeUtil.between(now, LocalDateTime.now()));
        System.out.println(proxy.aaaa("轻语", "yyj"));
        System.out.println(proxy.aaaa("轻语", "yyj"));
        proxy.bbbb("轻语");
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
