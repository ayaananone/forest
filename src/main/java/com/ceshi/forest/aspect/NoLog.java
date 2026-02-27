package com.ceshi.forest.aspect;

import java.lang.annotation.*;

/**
 * 标记此方法/类不记录日志
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoLog {
}