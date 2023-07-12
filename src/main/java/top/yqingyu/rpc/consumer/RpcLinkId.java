package top.yqingyu.rpc.consumer;

import java.util.concurrent.ConcurrentHashMap;

public class RpcLinkId {
    ConcurrentHashMap<Thread, String> RPC_LINK_ID_MAP = new ConcurrentHashMap<>();

    void setLinkId(String id) {
        RPC_LINK_ID_MAP.put(Thread.currentThread(), id);
    }

    void setLinkId(Thread th, String id) {
        RPC_LINK_ID_MAP.put(th, id);
    }

    String getLinkId() {
        return getLinkId(Thread.currentThread());
    }

    String getLinkId(Thread th) {
        return RPC_LINK_ID_MAP.get(th);
    }

}
