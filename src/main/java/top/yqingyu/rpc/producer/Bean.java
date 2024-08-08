package top.yqingyu.rpc.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Bean {
    static final Logger logger = LoggerFactory.getLogger(Bean.class);
    Object object;
    Method method;
    QyRpcInterceptorChain chain;

    public void invoke() throws Exception {
        chain.doChain();
    }

    public void invoke0() throws InvocationTargetException, IllegalAccessException {
        ProducerCtx ctx = ProducerCtx.getCtx();
        try {
            if (logger.isDebugEnabled())
                logger.debug("invoking from:{} is:{}", ctx.from, ctx.invokeStr);
            ctx.rtn = method.invoke(object, ctx.args);
            if (logger.isDebugEnabled())
                logger.debug("invoked suc from:{} is:{}", ctx.from, ctx.invokeStr);
        } catch (Throwable e) {
            if (logger.isDebugEnabled())
                logger.debug("invoked err from:{} is:{} msg:{}", ctx.from, ctx.invokeStr, e.getMessage());
            throw e;
        }
    }
}
