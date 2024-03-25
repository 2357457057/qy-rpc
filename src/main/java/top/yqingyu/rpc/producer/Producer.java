package top.yqingyu.rpc.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.qymsg.netty.MsgServer;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.annontation.QyRpcProducer;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Producer {
    public static final Logger logger = LoggerFactory.getLogger(Producer.class);
    static int serviceIdentifierTag = "QyRpcProducer".hashCode();
    final ConcurrentHashMap<String, Bean> ROUTING_TABLE = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, String> RPC_LINK_ID = new ConcurrentHashMap<>();
    volatile MsgServer msgServer;
    final String serverName;
    final ServerExceptionHandler exceptionHandler;
    final QyRpcInterceptorChain interceptorChain;
    private final int pool;
    private final int port;
    private final int radix;
    private final int clearTime;
    private final int bodyLengthMax;
    private final String threadName;

    private Producer(Builder builder) {
        serverName = builder.serverName;
        exceptionHandler = builder.exceptionHandler;
        interceptorChain = builder.interceptorChain;
        pool = builder.pool;
        radix = builder.radix;
        clearTime = builder.clearTime;
        bodyLengthMax = builder.bodyLengthMax;
        threadName = builder.threadName;
        port = builder.port;
    }

    /**
     * 注册这个对象到RPC服务
     * 服局将采用此对象执行相应的方法
     */
    public void register(Object o) throws ClassNotFoundException {
        Class<?> aClass = o.getClass();
        QyRpcProducer annotation = aClass.getAnnotation(QyRpcProducer.class);
        Class<?> interface_ = null;
        if (annotation == null) {
            for (Class<?> anInterface : aClass.getInterfaces()) {
                QyRpcProducer annotationT = anInterface.getAnnotation(QyRpcProducer.class);
                if (annotationT != null) {
                    interface_ = anInterface;
                    annotation = annotationT;
                }
            }
        }
        if (annotation == null) return;
        List<Class<?>> classArrayList = getAllClass(aClass);
        String className = RpcUtil.getClassName(aClass);
        if (className.contains(Constants.SpringCGLib)) {
            String[] split = className.split(Constants.SpringCGLibRegx);
            aClass = Class.forName(split[0]);
            className = RpcUtil.getClassName(aClass);
        }
        if (interface_ != null && className.contains(Constants.JDK_PROXY)) {
            className = interface_.getName();
        }
        for (Class<?> clazz : classArrayList) {
            regMethod(className, clazz, o);
        }
    }

    /**
     * 注册这个包下所有的类
     * 采用无对象构造函数创建对象
     * 服局将采用此对象执行相应的方法
     */
    public void register(String packageName) throws Exception {
        List<Class<?>> list = ClazzUtil.getClassListByAnnotation(packageName, QyRpcProducer.class);
        for (Class<?> aClass : list) {
            Constructor<?> constructor = aClass.getConstructor();
            if (constructor.trySetAccessible()) {
                constructor.setAccessible(true);
            }
            register(constructor.newInstance());
        }
    }

    public static String getLinkId() {
        return RPC_LINK_ID.get(Thread.currentThread().getName());
    }

    public static String getLinkId(String th) {
        return RPC_LINK_ID.get(th);
    }

    public void start() throws Exception {
        this.msgServer = new MsgServer.Builder().handler(RpcHandler.class, this).radix(radix).pool(pool).serverName(serverName).bodyLengthMax(bodyLengthMax).threadName(threadName).clearTime(clearTime).build();
        msgServer.start(port);
    }

    public void shutdown() throws InterruptedException {
        if (msgServer != null) {
            this.msgServer.shutdown();
        }
        logger.info("qyrpc producer is shutdown");
    }

    void serviceIdentifier(String s) {
        serviceIdentifierTag ^= s.hashCode();
    }

    private void regMethod(String className, Class<?> aClass, Object invoke) {
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            StringBuilder sb = new StringBuilder(className).append(Constants.method).append(method.getName());
            if (method.trySetAccessible()) {
                method.setAccessible(true);
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                sb.append(Constants.param).append(parameterType.getName());
            }
            Bean bean = new Bean();
            bean.method = method;
            bean.object = invoke;
            bean.chain = interceptorChain;
            String string = sb.toString();
            ROUTING_TABLE.put(string, bean);
            serviceIdentifier(string);
        }
    }

    private List<Class<?>> getAllClass(Class<?> clazz) {
        LinkedList<Class<?>> queue = new LinkedList<>();
        queue.add(clazz);
        ArrayList<Class<?>> list = new ArrayList<>();
        Class<?> poll = null;
        for (; ; ) {
            poll = queue.poll();
            if (poll == null) break;
            list.add(poll);
            queue.addAll(Arrays.asList(poll.getInterfaces()));
        }
        return list;
    }

    public static final class Builder {
        public QyRpcInterceptorChain interceptorChain = new QyRpcInterceptorChain();
        String serverName = "QyRpcProducer";
        ServerExceptionHandler exceptionHandler = new ServerExceptionHandler() {
        };
        private int pool = Runtime.getRuntime().availableProcessors() * 2;
        private int radix = 32;
        private int port = 4729;
        private int clearTime = 30 * 60 * 1000;
        private int bodyLengthMax = 1400;
        private String threadName = "handle";

        private Builder() {
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder serverName(String val) {
            serverName = val;
            return this;
        }

        public Builder exceptionHandler(ServerExceptionHandler val) {
            exceptionHandler = val;
            return this;
        }

        public Builder interceptorChain(QyRpcInterceptorChain val) {
            interceptorChain = val;
            return this;
        }

        public Builder pool(int val) {
            pool = val;
            return this;
        }

        public Builder radix(int val) {
            radix = val;
            return this;
        }

        public Builder clearTime(int val) {
            clearTime = val;
            return this;
        }

        public Builder bodyLengthMax(int val) {
            bodyLengthMax = val;
            return this;
        }

        public Builder threadName(String val) {
            threadName = val;
            return this;
        }

        public Builder port(int val) {
            port = val;
            return this;
        }

        public Producer build() throws Exception {
            return new Producer(this);
        }
    }
}
