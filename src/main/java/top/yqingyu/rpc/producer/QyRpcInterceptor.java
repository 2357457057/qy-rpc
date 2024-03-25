package top.yqingyu.rpc.producer;


public interface QyRpcInterceptor {
    void pre();

    void post();
}