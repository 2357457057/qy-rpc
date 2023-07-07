package top.yqingyu.rpc.annontation;

import java.lang.annotation.*;

/**
 * 用与配置该rpc接口的重试次数以及
 * 以及每次最大等待时间
 * retryTimes <=0 或 waitTime <= 0 为无效值
 * 所以最终的等待时间是 retryTimes * waitTime 毫秒
 * 要注意 方法上的参数将会完全重写类上的参数
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface QyRpcProducerProperties {
    long waitTime() default 0;

    int retryTimes() default 0;

    boolean retryDiffProducer() default true;
}
