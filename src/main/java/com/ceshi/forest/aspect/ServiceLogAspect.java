package com.ceshi.forest.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Service 层日志切面
 * 统一记录业务层方法调用日志
 */
@Slf4j
@Aspect
@Component
public class ServiceLogAspect {

    /**
     * 定义切点：所有 Service 实现类的方法
     */
    @Pointcut("execution(* com.ceshi.forest.service.impl.*.*(..))")
    public void serviceMethods() {}

    /**
     * 排除标记了 @NoLog 的方法
     */
    @Pointcut("@annotation(com.ceshi.forest.aspect.NoLog)")
    public void noLogMethods() {}

    /**
     * 环绕通知：记录 Service 层方法调用
     */
    @Around("serviceMethods() && !noLogMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String fullMethodName = className + "." + methodName;

        Object[] args = joinPoint.getArgs();
        String argsStr = formatArgs(args);

        long startTime = System.currentTimeMillis();
        log.info("[Service] 开始: {}({})", fullMethodName, argsStr);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            String resultStr = formatResult(result);
            log.info("[Service] 成功: {}({}), 耗时: {}ms, 返回: {}",
                    fullMethodName, argsStr, duration, resultStr);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[Service] 失败: {}({}), 耗时: {}ms, 错误: {}",
                    fullMethodName, argsStr, duration, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 格式化参数
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    if (arg instanceof String) return "\"" + arg + "\"";
                    if (arg instanceof Number) return arg.toString();
                    return arg.getClass().getSimpleName();
                })
                .limit(3) // 最多显示3个参数
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    /**
     * 格式化结果
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof java.util.List) {
            return "List[" + ((java.util.List<?>) result).size() + "]";
        }
        if (result instanceof java.util.Map) {
            return "Map[" + ((java.util.Map<?, ?>) result).size() + "]";
        }
        return result.getClass().getSimpleName();
    }
}