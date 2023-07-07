package top.yqingyu.rpc.consumer;

import top.yqingyu.common.cglib.proxy.Enhancer;
import top.yqingyu.common.cglib.proxy.MethodInterceptor;
import top.yqingyu.common.cglib.proxy.MethodProxy;
import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.qymsg.DataType;
import top.yqingyu.qymsg.MsgHelper;
import top.yqingyu.qymsg.MsgType;
import top.yqingyu.qymsg.QyMsg;
import top.yqingyu.qymsg.netty.Connection;
import top.yqingyu.rpc.Dict;
import top.yqingyu.rpc.exception.RpcException;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransRpc {
    private static final ConcurrentHashMap<String, ConsumerHolder> CONSUMER_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> INVOKE_HIS_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) throws Throwable {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new MethodExecute(clazz));
        return (T) enhancer.create();
    }


    static void addConsumer(Consumer consumer) throws Exception {
        Connection connection = consumer.getClient().getConnection();
        QyMsg qyMsg = new QyMsg(MsgType.AC, DataType.OBJECT);
        QyMsg back = connection.get(qyMsg);
        String s = MsgHelper.gainMsgValue(back, Dict.serviceIdentifierTag);

        if (!CONSUMER_MAP.containsKey(s)) {
            ConsumerHolder holder = new ConsumerHolder();
            holder.add(consumer);
            CONSUMER_MAP.put(s, holder);
            return;
        }
        ConsumerHolder holder = CONSUMER_MAP.get(s);
        holder.add(consumer);
    }

    private static class ConsumerHolder {
        final List<Consumer> consumerList = new ArrayList<>();
        final AtomicInteger i = new AtomicInteger();

        Consumer next() {
            int idx = Math.abs(i.getAndIncrement()) % consumerList.size();
            return consumerList.get(idx);
        }

        void add(Consumer c) {
            consumerList.add(c);
        }

    }

    private static class MethodExecute implements MethodInterceptor {

        Class<?> proxyClass;

        public MethodExecute(Class<?> proxyClass) {
            this.proxyClass = proxyClass;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] param, MethodProxy proxy) throws Throwable {
            String className = RpcUtil.getClassName(proxyClass);
            String name = method.getName();
            StringBuilder sb = new StringBuilder(className).append(Dict.method).append(name);
            if (param != null)
                for (Object o : param) {
                    sb.append("#").append(o.getClass().getName());
                }
            String string = sb.toString();
            String s = INVOKE_HIS_MAP.get(string);
            Consumer consumer = null;
            QyMsg qyMsg = new QyMsg(MsgType.NORM_MSG, DataType.OBJECT);
            qyMsg.putMsg(string);
            qyMsg.putMsgData(Dict.parameterList, param);
            if (StringUtil.isNotEmpty(s)) {
                consumer = CONSUMER_MAP.get(s).next();
            }
            if (consumer != null) {
                qyMsg.setFrom(consumer.getName());
                QyMsg back = consumer.getClient().getConnection().get(qyMsg);
                String type = MsgHelper.gainMsg(back);
                switch (type) {
                    case Dict.invokeSuccess -> {
                        return back.getDataMap().get(Dict.invokeResult);
                    }
                    case Dict.invokeNoSuch -> {
                        return null;
                    }
                    case Dict.invokeThrowError -> {
                        throw new Throwable("remote process error", (Throwable) back.getDataMap().get(Dict.invokeResult));
                    }
                    default -> {
                        throw new RpcException("unknown type {}", type);
                    }
                }
            }
            QyMsg back = null;
            Iterator<String> keys = CONSUMER_MAP.keys().asIterator();
            while (keys.hasNext()) {
                String next = keys.next();
                ConsumerHolder h = CONSUMER_MAP.get(next);
                Consumer c = h.next();
                qyMsg.setFrom(c.getName());
                try {
                    back = c.getClient().getConnection().get(qyMsg);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                String type = MsgHelper.gainMsg(back);

                switch (type) {
                    case Dict.invokeSuccess -> {
                        INVOKE_HIS_MAP.put(string, next);
                        return back.getDataMap().get(Dict.invokeResult);
                    }
                    case Dict.invokeNoSuch -> {
                        continue;
                    }
                    case Dict.invokeThrowError -> {
                        INVOKE_HIS_MAP.put(string, next);
                        throw new Throwable("remote process error", (Throwable) back.getDataMap().get(Dict.invokeResult));
                    }
                }
            }
            return null;
        }
    }
}
