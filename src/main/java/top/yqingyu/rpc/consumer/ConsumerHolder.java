package top.yqingyu.rpc.consumer;


import top.yqingyu.common.cglib.core.DuplicatesPredicate;


import top.yqingyu.rpc.exception.IllegalRpcArgumentException;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerHolder {
    final List<Consumer> consumerList = new ArrayList<>();
    final AtomicInteger i = new AtomicInteger();
    final String serverTag;
    final ConsumerHolderContext ctx;

    public ConsumerHolder(String serverTag, ConsumerHolderContext ctx) {
        this.serverTag = serverTag;
        this.ctx = ctx;
    }


    Consumer next() {
        int idx = Math.abs(i.getAndIncrement()) % consumerList.size();
        return consumerList.get(idx);
    }

    void add(Consumer c, String tag) {
        if (!serverTag.equals(tag))
            throw new IllegalRpcArgumentException("序:{} 名:{} 服务配置的服务是不同的，请检查", consumerList.size() + 1, c.name);
        consumerList.add(c);
    }




}
