package top.yqingyu.rpc.annontation;

import java.lang.annotation.*;

/**
 * 代表此类为提供方
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QyRpcProducer {
}
