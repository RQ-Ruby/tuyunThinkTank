package com.RQ.tuyunthinktank.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /**
     * 限流的唯一键名（Redis Key 前缀）
     */
    String key() default "rate_limit";

    /**
     * 时间窗口，单位：秒
     */
    int time() default 60;

    /**
     * 允许请求的最大次数
     */
    int count() default 10;
}