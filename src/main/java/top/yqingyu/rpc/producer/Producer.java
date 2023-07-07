package top.yqingyu.rpc.producer;

import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.qymsg.netty.MsgServer;
import top.yqingyu.rpc.Dict;
import top.yqingyu.rpc.annontation.QyRpcProducer;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Producer {
    byte[] serviceIdentifierTag = "QyRpcProducer".repeat(24).getBytes();
    final ConcurrentHashMap<String, Bean> ROUTING_TABLE = new ConcurrentHashMap<>();
    MsgServer msgServer;
    final String serverName;
    final ServerExceptionHandler exceptionHandler;
    private final int pool;
    private final int port;
    private final int radix;
    private final int clearTime;
    private final int bodyLengthMax;
    private final String threadName;

    private Producer(Builder builder) {
        serverName = builder.serverName;
        exceptionHandler = builder.exceptionHandler;
        pool = builder.pool;
        radix = builder.radix;
        clearTime = builder.clearTime;
        bodyLengthMax = builder.bodyLengthMax;
        threadName = builder.threadName;
        port = builder.port;
    }


    public void register(Object o) {
        Class<?> aClass = o.getClass();
        QyRpcProducer annotation = aClass.getAnnotation(QyRpcProducer.class);
        if (annotation == null) return;

        String className = RpcUtil.getClassName(aClass);
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            StringBuilder sb = new StringBuilder(className).append(Dict.method).append(method.getName());
            if (method.trySetAccessible()) {
                method.setAccessible(true);
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                sb.append(Dict.param).append(parameterType.getName());
            }
            Bean bean = new Bean();
            bean.method = method;
            bean.object = o;
            String string = sb.toString();
            ROUTING_TABLE.put(string, bean);
            serviceIdentifier(string);
        }
    }

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

    public void start() throws Exception {
        this.msgServer = new MsgServer.Builder()
                .handler(RpcHandler.class, this)
                .radix(radix)
                .pool(pool)
                .serverName(serverName)
                .bodyLengthMax(bodyLengthMax)
                .threadName(threadName)
                .clearTime(clearTime)
                .build();
        msgServer.start(port);
    }

    void serviceIdentifier(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int min = Math.min(bytes.length, serviceIdentifierTag.length);
        for (int i = 0; i < min; i++) {
            serviceIdentifierTag[i] = (byte) (serviceIdentifierTag[i] | bytes[i]);
        }
    }

    public static final class Builder {
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
