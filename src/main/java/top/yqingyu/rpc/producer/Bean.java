package top.yqingyu.rpc.producer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Bean {
    Object object;
    Method method;
    QyRpcInterceptorChain chain;

    public void invoke() throws Exception {
        chain.doChain();
    }

    public void invoke0() throws InvocationTargetException, IllegalAccessException {
        ProducerCtx ctx = ProducerCtx.getCtx();
        ctx.rtn = method.invoke(object, ctx.args);
    }
}
