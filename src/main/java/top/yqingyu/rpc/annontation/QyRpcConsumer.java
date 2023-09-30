package top.yqingyu.rpc.annontation;

import java.lang.annotation.*;

/**
 *
 * 配合Spring使用。
 * 该注解已不再使用
 * 新版 qyrpc-springboot-starter 已经支持自动注入.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
public @interface QyRpcConsumer {
    String value() default "";
}
