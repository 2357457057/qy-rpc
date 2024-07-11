package top.yqingyu.rpc.consumer;

import top.yqingyu.common.cglib.proxy.MethodInterceptor;
import top.yqingyu.common.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLibMethodExecProxy extends MethodExecProxy implements MethodInterceptor {


    public CGLibMethodExecProxy(Class<?> proxyClass, String consumerName, ConsumerHolderContext ctx) {
        super(proxyClass, consumerName, ctx);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] param, MethodProxy proxy) throws Throwable {
        return invoke0(obj, method, param);
    }
}