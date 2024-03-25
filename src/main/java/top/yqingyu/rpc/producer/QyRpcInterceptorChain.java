package top.yqingyu.rpc.producer;

import top.yqingyu.common.cglib.core.ClassLoaderAwareGeneratorStrategy;
import top.yqingyu.common.cglib.core.QyNamingPolicy;
import top.yqingyu.common.cglib.proxy.Enhancer;
import top.yqingyu.common.cglib.proxy.MethodInterceptor;
import top.yqingyu.common.cglib.proxy.MethodProxy;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class QyRpcInterceptorChain {
    private final ThreadLocal<AtomicInteger> ID = ThreadLocal.withInitial(AtomicInteger::new);
    final ThreadLocal<Boolean> IN = ThreadLocal.withInitial(() -> true);
    final ThreadLocal<InterceptorCallBack> CALL_BACK = new ThreadLocal<>();
    private final List<QyRpcInterceptor> rpcInterceptorList = new ArrayList<>();


    public void addAll(Collection<QyRpcInterceptor> qyRpcInterceptors) {
        qyRpcInterceptors.forEach(this::add);
    }

    public void add(QyRpcInterceptor qyRpcInterceptor) {
        Enhancer enhancer = new Enhancer();
        Class<? extends QyRpcInterceptor> aClass = qyRpcInterceptor.getClass();
        enhancer.setSuperclass(aClass);
        enhancer.setNamingPolicy(QyNamingPolicy.INSTANCE);
        enhancer.setCallback(new ChainProxy(qyRpcInterceptor, this));
        enhancer.setAttemptLoad(true);
        ClassLoader classLoader = RpcUtil.getClassLoader(aClass);
        if (classLoader != null) enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(classLoader));
        rpcInterceptorList.add((QyRpcInterceptor) enhancer.create());
    }


    void doChain(InterceptorCallBack interceptorCallBack) {
        if (rpcInterceptorList.isEmpty()) return;
        CALL_BACK.set(interceptorCallBack);
        try {
            rpcInterceptorList.get(0).pre();
        } finally {
            ID.get().set(0);
            IN.set(true);
            CALL_BACK.remove();
        }
    }

    QyRpcInterceptor next(InterceptorCallBack interceptorCallBack) throws InvocationTargetException, IllegalAccessException {
        AtomicInteger id = ID.get();

        int i = id.incrementAndGet();

        if (i == rpcInterceptorList.size()) {
            IN.set(false);
        }

        if (IN.get()) {
            return rpcInterceptorList.get(i);
        }

        if (interceptorCallBack != null) interceptorCallBack.call();
        id.getAndDecrement();

        int andDecrement = id.getAndDecrement();
        return andDecrement >= 0 ? rpcInterceptorList.get(andDecrement) : null;
    }

    private record ChainProxy(QyRpcInterceptor _this, QyRpcInterceptorChain chain) implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if ("pre".equals(method.getName())) {
                method.invoke(_this, args);
                QyRpcInterceptor interceptor = chain.next(chain.CALL_BACK.get());
                if (interceptor == null) return null;
                if (chain.IN.get()) {
                    interceptor.pre();
                    return null;
                }
                interceptor.post();
            } else if ("post".equals(method.getName())) {
                method.invoke(_this, args);
                QyRpcInterceptor next = chain.next(null);
                if (next == null) return null;
                next.post();
            } else {
                method.invoke(_this, args);
            }
            return null;
        }
    }
}
