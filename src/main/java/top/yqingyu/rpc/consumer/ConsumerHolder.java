package top.yqingyu.rpc.consumer;

import top.yqingyu.common.cglib.proxy.Enhancer;
import top.yqingyu.rpc.exception.IllegalRpcArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerHolder {
    final List<Consumer> consumerList = new ArrayList<>();
    final AtomicInteger i = new AtomicInteger();
    final ConcurrentHashMap<Class<?>, Object> ProxyClassCache = new ConcurrentHashMap<>();
    final String serverTag;


    public ConsumerHolder(String serverTag) {
        this.serverTag = serverTag;
    }


    Consumer next() {
        int idx = Math.abs(i.getAndIncrement()) % consumerList.size();
        return consumerList.get(idx);
    }

    void add(Consumer c, String tag) {
        if (!serverTag.equals(tag))
            throw new IllegalRpcArgumentException("序:{} 名:{} 服务配置的服务是不同的，请检查", consumerList.size() + 1, c.name);
        consumerList.add(c);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        if (ProxyClassCache.containsKey(clazz)) {
            return (T) ProxyClassCache.get(clazz);
        }
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new ProxyClassMethodExecutor(clazz, this));
        T t = (T) enhancer.create();
        ProxyClassCache.put(clazz, t);
        return t;
    }
}
