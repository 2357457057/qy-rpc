package top.yqingyu.rpc.producer;

import top.yqingyu.common.utils.ClazzUtil;
import top.yqingyu.qymsg.netty.MsgServer;
import top.yqingyu.rpc.Dict;
import top.yqingyu.rpc.annontation.TransRpc;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Producer {
    byte[] serviceIdentifierTag = "QyRpc".repeat(24).getBytes();
    final ConcurrentHashMap<String, Bean> ROUTING_TABLE = new ConcurrentHashMap<>();
    MsgServer msgServer;
    String serverName = "QyRpcProducer";
    ServerExceptionHandler exceptionHandler = new ServerExceptionHandler() {
    };

    Producer() {
    }

    public static Producer create(int radix, int port) throws Exception {
        Producer producer = new Producer();
        MsgServer build = new MsgServer.Builder()
                .handler(RpcHandler.class, producer)
                .radix(radix)
                .build();
        build.start(port);
        producer.msgServer = build;
        return producer;
    }

    public static Producer create(int radix, int port, int threadNum) throws Exception {
        Producer producer = new Producer();
        MsgServer build = new MsgServer.Builder()
                .handler(RpcHandler.class, producer)
                .radix(radix)
                .pool(threadNum)
                .build();
        build.start(port);
        producer.msgServer = build;
        return producer;
    }

    public static Producer create(int radix, int port, int threadNum, ServerExceptionHandler exceptionHandler) throws Exception {
        Producer producer = new Producer();
        producer.exceptionHandler = exceptionHandler;
        MsgServer build = new MsgServer.Builder()
                .handler(RpcHandler.class, producer)
                .radix(radix)
                .pool(threadNum)
                .build();
        build.start(port);
        producer.msgServer = build;
        return producer;
    }

    public static Producer create(int radix, int port, ServerExceptionHandler exceptionHandler) throws Exception {
        Producer producer = new Producer();
        producer.exceptionHandler = exceptionHandler;
        MsgServer build = new MsgServer.Builder()
                .handler(RpcHandler.class, producer)
                .radix(radix)
                .build();
        build.start(port);
        producer.msgServer = build;
        return producer;
    }


    public void register(Object o) {
        Class<?> aClass = o.getClass();
        TransRpc annotation = aClass.getAnnotation(TransRpc.class);
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
        List<Class<?>> list = ClazzUtil.getClassListByAnnotation(packageName, TransRpc.class);
        for (Class<?> aClass : list) {
            Constructor<?> constructor = aClass.getConstructor();
            if (constructor.trySetAccessible()) {
                constructor.setAccessible(true);
            }
            register(constructor.newInstance());
        }
    }


    void serviceIdentifier(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int min = Math.min(bytes.length, serviceIdentifierTag.length);
        for (int i = 0; i < min; i++) {
            serviceIdentifierTag[i] = (byte) (serviceIdentifierTag[i] | bytes[i]);
        }
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
