package com.RQ.tuyunthinktank.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @description
 * @author RQ
 * @date 2025/6/2 上午11:51
 */
//Target用于描述注解的使用范围（即：被描述的注解可以用在什么地方
    //ElementType.METHOD 方法上
@Target({ElementType.METHOD})
//Retention用于描述注解的生命周期（即：被描述的注解在什么范围内有效）
    //RetentionPolicy.RUNTIME 运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
//    必须有某个角色
    String mustRole() default "";

}
