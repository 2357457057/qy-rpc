package top.yqingyu.rpc.annontation;

import java.lang.annotation.*;

/**
 * 配合Spring使用。
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QyRpcConsumer {
    String value() default "";
}
