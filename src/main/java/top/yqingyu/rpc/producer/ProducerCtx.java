package top.yqingyu.rpc.producer;

import java.lang.reflect.Method;

public class ProducerCtx {
    private final static ThreadLocal<ProducerCtx> CTX = new ThreadLocal<>();
    private final String rpcLinkId;
    Bean bean;
    Object[] args;
    Object rtn;
    String from;

    ProducerCtx() {
        this.rpcLinkId = Producer.getLinkId();
        CTX.set(this);
    }

    public static String getRpcLinkId() {
        return CTX.get().rpcLinkId;
    }

    public static Object getSpringBean() {
        return CTX.get().bean.object;
    }

    public static Object[] getArgs() {
        return CTX.get().args;
    }

    public static Method getMethod() {
        return CTX.get().bean.method;
    }

    public static Object getRtn() {
        return CTX.get().rtn;
    }

    public static Object getFrom() {
        return CTX.get().from;
    }

    public static ProducerCtx getCtx() {
        return CTX.get();
    }

    static void remove() {
        CTX.remove();
    }

}
