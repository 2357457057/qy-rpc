# QyRpc

#### 介绍
基于QyMsg 传输协议构建的轻量Rpc框架


#### 安装教程

1.  在mvn官网 搜索  qyrpc
2.  引入最新版本的 mvn坐标 到你的项目中
```xml
  <!-- https://mvnrepository.com/artifact/top.yqingyu/QyRpc -->
<dependency>
    <groupId>top.yqingyu</groupId>
    <artifactId>QyRpc</artifactId>
    <version>1.9.7</version>
</dependency>
```

#### 使用说明
 1、创建 服务端
 ```java
import top.yqingyu.rpc.annontation.QyRpcProducer;
import top.yqingyu.rpc.producer.Producer;

@QyRpcProducer
public class A {
    public static void main(String[] args) throws Exception {
        Producer producer = Producer.Builder.newBuilder()
                .port(4737)
                .build();
        producer.start();
        producer.register(new A());
        Thread.sleep(9000000);
    }

    public String aaaa(String aa) {
        System.out.println("你好呀" + aa);
        return aa + "说：小苏你妈蛋";
    }

    public void bbbb(String cc) {
        System.out.println(cc + "远程来访");
        throw new RuntimeException("红温模式。。。。。。。。。。。。。。。。。。。。。。。。。");
    }
}

 ```
2、创建 消费端
```java
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
                .port(4737)
                .build();
        Consumer consumer = Consumer.create(build, consumerHolderContext);
        A proxy = consumerHolderContext.getProxy(consumer.getName(), A.class);
        proxy.bbbb("轻语");
        // remoteHandle(consumerHolderContext, consumer);
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
                    log.info("{}", proxy.getIpInfo("96.201.45.89"));
                } catch (Exception e) {
                    log.error("", e);
                }
            }).start();
        }
    }
}

```
#### 参与贡献

1.  Fork 本仓库
2.  新建 feature_xxx 分支
3.  提交代码
4.  新建 Pull Request

