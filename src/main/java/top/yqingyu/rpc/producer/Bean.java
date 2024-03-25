package top.yqingyu.rpc.producer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

class Bean {
    Object object;
    Method method;
    QyRpcInterceptorChain chain;

    public Object invoke(Object... objs) throws Exception {
        ProducerCtx.getCtx().args = objs;
        chain.doChain();
        return ProducerCtx.getRtn();
    }

    public Object invoke0() throws InvocationTargetException, IllegalAccessException {
        return ProducerCtx.getMethod().invoke(ProducerCtx.getSpringBean(),ProducerCtx.getArgs());
    }
}
