package top.yqingyu.rpc.producer;

import java.lang.reflect.Method;

public class Bean {
    Object object;
    Method method;

    public Object invoke(Object... objs) throws Exception {
        return method.invoke(object, objs);
    }
}
