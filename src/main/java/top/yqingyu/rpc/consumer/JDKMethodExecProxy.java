package top.yqingyu.rpc.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JDKMethodExecProxy extends MethodExecProxy implements InvocationHandler {
    public JDKMethodExecProxy(Class<?> proxyClass, String consumerName, ConsumerHolderContext ctx) {
        super(proxyClass, consumerName, ctx);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return super.invoke0(proxy, method, args);
    }

}
