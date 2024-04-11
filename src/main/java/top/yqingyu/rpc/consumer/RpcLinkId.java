package top.yqingyu.rpc.consumer;

import java.util.concurrent.ConcurrentHashMap;

class RpcLinkId {
    ConcurrentHashMap<String, String> RPC_LINK_ID_MAP = new ConcurrentHashMap<>();
    ThreadLocal<String> RPC_LINK_ID_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        String name = "RemoteTh-" + Thread.currentThread().getName();
        return name.length() > 32 ? name.substring(0, 32): name;
    });

    void setLinkId(String id) {
        RPC_LINK_ID_MAP.put(Thread.currentThread().getName(), id);
    }

    void setLinkId(String th, String id) {
        RPC_LINK_ID_MAP.put(th, id);
    }

    String getLinkId() {
        return getLinkId(Thread.currentThread().getName());
    }

    String getLinkId(String th) {
        String s = RPC_LINK_ID_MAP.get(th);
        return s == null ? RPC_LINK_ID_THREAD_LOCAL.get() : s;
    }

    void removeLinkId(String th) {
        RPC_LINK_ID_MAP.remove(th);
    }

    void removeLinkId() {
        removeLinkId(Thread.currentThread().getName());
    }
}
