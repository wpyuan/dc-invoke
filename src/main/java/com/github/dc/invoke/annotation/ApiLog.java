package com.github.dc.invoke.annotation;


import com.github.dc.invoke.aop.handler.IApiLogDataHandler;

import java.lang.annotation.*;

/**
 * @author PeiYuan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface ApiLog {
    /**
     * 接口代码
     */
    String code() default "接口代码";
    /**
     * 接口描述
     */
    String desc() default "接口描述";

    /**
     * 该日志对应的业务主键.支持SpEL
     * @return
     */
    String businessKey() default "";

    Class<? extends IApiLogDataHandler> handler();

    /**
     * 是否打印响应内容
     * @return
     */
    boolean printResponse() default false;
}
