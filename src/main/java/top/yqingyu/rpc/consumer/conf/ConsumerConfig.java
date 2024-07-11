package top.yqingyu.rpc.consumer.conf;

import top.yqingyu.common.utils.UUIDUtil;
import top.yqingyu.qymsg.netty.ConnectionConfig;

public class ConsumerConfig {
    /**
     * 默认使用混合代理，接口优先JDK
     * 类使用CGLib
     */
    public ProxyMode proxyMode = ProxyMode.CGlib;
    public boolean enableRegister = false;
    public String id = UUIDUtil.randomUUID().toString2();
    public String host = "127.0.0.1";
    public int port = 4728;
    public int poolMax = 10;
    public int poolMin = 2;
    public int radix = 32;
    public int bodyLengthMax = 1024 * 1024 * 10;
    public String name = "QyRpcConsumer";
    public String threadName = "handle";
    volatile long clearTime = 30 * 60 * 1000;

    public ConsumerConfig() {

    }

    public ConnectionConfig getConnectionConfig() {
        return new ConnectionConfig.Builder()
                .host(host)
                .port(port)
                .poolMax(poolMax)
                .poolMin(poolMin)
                .radix(radix)
                .bodyLengthMax(bodyLengthMax)
                .name(name)
                .threadName(threadName)
                .clearTime(clearTime)
                .build();
    }

    public ProxyMode getProxyMode() {
        return proxyMode;
    }

    public void setProxyMode(ProxyMode proxyMode) {
        this.proxyMode = proxyMode;
    }

    public boolean isEnableRegister() {
        return enableRegister;
    }

    public void setEnableRegister(boolean enableRegister) {
        this.enableRegister = enableRegister;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPoolMax() {
        return poolMax;
    }

    public void setPoolMax(int poolMax) {
        this.poolMax = poolMax;
    }

    public int getPoolMin() {
        return poolMin;
    }

    public void setPoolMin(int poolMin) {
        this.poolMin = poolMin;
    }

    public int getRadix() {
        return radix;
    }

    public void setRadix(int radix) {
        this.radix = radix;
    }

    public int getBodyLengthMax() {
        return bodyLengthMax;
    }

    public void setBodyLengthMax(int bodyLengthMax) {
        this.bodyLengthMax = bodyLengthMax;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getClearTime() {
        return clearTime;
    }

    public void setClearTime(long clearTime) {
        this.clearTime = clearTime;
    }
}
