package top.yqingyu.rpc.producer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QyRpcInterceptorChain {

    private final List<QyRpcInterceptor> rpcInterceptorList = new ArrayList<>();

    public void addAll(Collection<QyRpcInterceptor> qyRpcInterceptors) {
        qyRpcInterceptors.forEach(this::add);
    }

    public void add(QyRpcInterceptor qyRpcInterceptor) {
        rpcInterceptorList.add(qyRpcInterceptor);
    }


    void doChain() throws InvocationTargetException, IllegalAccessException {
        pre();
        ProducerCtx.getCtx().bean.invoke0();
        post();
    }

    void pre() {
        for (QyRpcInterceptor interceptor : rpcInterceptorList) {
            interceptor.pre();
        }
    }

    void post() {
        for (int i = rpcInterceptorList.size() - 1; i >= 0; i--) {
            rpcInterceptorList.get(i).post();
        }
    }
}
