package top.yqingyu.rpc.consumer;

import top.yqingyu.common.cglib.core.ClassLoaderAwareGeneratorStrategy;
import top.yqingyu.common.cglib.core.DuplicatesPredicate;
import top.yqingyu.common.cglib.core.QyNamingPolicy;
import top.yqingyu.common.cglib.proxy.CallbackFilter;
import top.yqingyu.common.cglib.proxy.Enhancer;
import top.yqingyu.common.cglib.proxy.MethodInterceptor;
import top.yqingyu.common.cglib.proxy.MethodProxy;
import top.yqingyu.rpc.exception.IllegalRpcArgumentException;

import java.lang.reflect.Method;
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
        enhancer.setNamingPolicy(QyNamingPolicy.INSTANCE);
        enhancer.setCallback(new ProxyClassMethodExecutor(clazz, this));
        enhancer.setAttemptLoad(true);
        ClassLoader classLoader = getClassLoader(clazz);
        if (classLoader != null)
            enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));
        T t = (T) enhancer.create();
        ProxyClassCache.put(clazz, t);
        return t;
    }

    private static ClassLoader getClassLoader(Class c) {
        ClassLoader cl = c.getClassLoader();
        if (cl == null) {
            cl = DuplicatesPredicate.class.getClassLoader();
        }
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        return cl;
    }
}
