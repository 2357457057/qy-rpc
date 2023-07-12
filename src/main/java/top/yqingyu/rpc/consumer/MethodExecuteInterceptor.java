package top.yqingyu.rpc.consumer;

import java.lang.reflect.Method;

public interface MethodExecuteInterceptor {
    default void before(ConsumerHolderContext ctx, Method method, Object[] param) {
    }

    default void error(ConsumerHolderContext ctx, Method method, Object[] param, Throwable error) {
    }

    default void completely(ConsumerHolderContext ctx, Method method, Object[] param, Object result) {
    }
}
