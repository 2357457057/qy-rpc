package top.yqingyu.rpc.consumer;

import java.util.concurrent.ConcurrentHashMap;

class RpcLinkId {
    ConcurrentHashMap<String, String> RPC_LINK_ID_MAP = new ConcurrentHashMap<>();

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
        return RPC_LINK_ID_MAP.get(th);
    }

    void removeLinkId(String th) {
        RPC_LINK_ID_MAP.remove(th);
    }

    void removeLinkId() {
        removeLinkId(Thread.currentThread().getName());
    }
}
