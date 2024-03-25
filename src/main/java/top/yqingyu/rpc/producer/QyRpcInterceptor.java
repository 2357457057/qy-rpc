package top.yqingyu.rpc.producer;


import java.lang.reflect.Method;

public interface QyRpcInterceptor {
    /**
     * 前置
     */
    void pre();

    /**
     * 后置
     */
    void post();
}