package com.example.annotion;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatExecuteLimit {

    /**
     * 业务名称前缀
     */
    String name() default "";

    /**
     * SpEL 表达式参数，用于提取方法参数中的动态值（如 #apiData.id）
     */
    String[] keys() default {};

    /**
     * 防重复时间窗口（秒），即在业务执行成功后，多长时间内不允许重复执行
     * 默认 5 秒
     */
    long durationTime() default 5;

    /**
     * 重复提交时的提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}
