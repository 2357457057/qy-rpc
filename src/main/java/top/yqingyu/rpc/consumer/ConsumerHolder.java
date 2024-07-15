package top.yqingyu.rpc.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.rpc.exception.IllegalRpcArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerHolder {
    final static Logger logger = LoggerFactory.getLogger(ConsumerHolder.class);
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
            throw new IllegalRpcArgumentException("seq:{} name:{} auth fail ,not same service, plz check", consumerList.size() + 1, c.getName());
        consumerList.add(c);
    }


}
