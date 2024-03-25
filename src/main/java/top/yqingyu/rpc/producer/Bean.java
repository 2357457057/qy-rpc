package top.yqingyu.rpc.producer;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

class Bean {
    Object object;
    Method method;
    QyRpcInterceptorChain chain;

    public Object invoke(Object... objs) throws Exception {
        AtomicReference<Object> rtn = new AtomicReference<>(null);
        chain.doChain(() -> rtn.set(method.invoke(object, objs)));
        return rtn.get();
    }
}
