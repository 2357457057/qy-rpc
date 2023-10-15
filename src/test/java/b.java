import lombok.extern.slf4j.Slf4j;

import top.yqingyu.qymsg.socket.ConnectionConfig;
import top.yqingyu.qyws.modules.web.mapper.ViewNumMapper;
import top.yqingyu.rpc.consumer.Consumer;
import top.yqingyu.rpc.consumer.ConsumerHolderContext;

@Slf4j
public class b {

    public static void main(String[] args) throws Throwable {
        ConsumerHolderContext consumerHolderContext = new ConsumerHolderContext();
        ConnectionConfig build = new ConnectionConfig.Builder().port(4736).build();
        Consumer consumer = Consumer.create(build, consumerHolderContext);
        ViewNumMapper proxy = consumerHolderContext.getProxy(consumer.getName(), ViewNumMapper.class);

        log.info(proxy.toString());

        consumerHolderContext.setLinkId("session5");
        log.info(proxy.getViewNum());
        consumerHolderContext.setLinkId("session2");
        log.info(proxy.getViewNum());
        log.info("{}", proxy.getTD_S_WEBSITE_ACCESS("127.0.0.1"));
        consumerHolderContext.setLinkId("session7");
        log.info("{}", proxy.getTD_S_WEBSITE_ACCESS("127.0.0.1"));
        consumerHolderContext.setLinkId("session4");
        log.info("{}", proxy.getTD_S_WEBSITE_ACCESS("127.0.0.1"));
    }
}
