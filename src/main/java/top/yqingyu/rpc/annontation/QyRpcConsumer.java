package top.yqingyu.rpc.annontation;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface QyRpcConsumer {
    String value();
}
