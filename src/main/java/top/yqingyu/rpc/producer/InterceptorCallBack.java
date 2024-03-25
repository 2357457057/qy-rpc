package top.yqingyu.rpc.producer;

import java.lang.reflect.InvocationTargetException;

public interface InterceptorCallBack {
    void call() throws InvocationTargetException, IllegalAccessException;
}
