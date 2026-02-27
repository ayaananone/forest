package com.ceshi.forest.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class ControllerLogAspect {

    /**
     * 定义切点：所有 Controller 类的方法
     */
    @Pointcut("execution(* com.ceshi.forest.controller.*.*(..))")
    public void controllerMethods() {}

    /**
     * 排除标记了 @NoLog 的方法
     */
    @Pointcut("@annotation(com.ceshi.forest.aspect.NoLog)")
    public void noLogMethods() {}

    /**
     * 环绕通知：记录请求和响应日志（排除 @NoLog 标记的方法）
     */
    @Around("controllerMethods() && !noLogMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String requestPath = buildRequestPath(method);
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        long startTime = System.currentTimeMillis();
        log.info("收到请求: {} [{}]", requestPath, methodName);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            String resultDesc = buildResultDescription(result);
            log.info("响应成功: {} [{}], 耗时: {}ms, {}", requestPath, methodName, duration, resultDesc);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("请求失败: {} [{}], 耗时: {}ms, 错误: {}",
                    requestPath, methodName, duration, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 为 @NoLog 方法提供简单的环绕通知（直接执行，不记录日志）
     */
    @Around("noLogMethods()")
    public Object noLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }

    private String buildRequestPath(Method method) {
        StringBuilder path = new StringBuilder();
        Class<?> clazz = method.getDeclaringClass();
        RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
        if (classMapping != null && classMapping.value().length > 0) {
            path.append(classMapping.value()[0]);
        }

        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        String methodPath = "";
        String httpMethod = "GET";

        if (getMapping != null) {
            methodPath = getMapping.value().length > 0 ? getMapping.value()[0] : "";
            httpMethod = "GET";
        } else if (postMapping != null) {
            methodPath = postMapping.value().length > 0 ? postMapping.value()[0] : "";
            httpMethod = "POST";
        } else if (putMapping != null) {
            methodPath = putMapping.value().length > 0 ? putMapping.value()[0] : "";
            httpMethod = "PUT";
        } else if (deleteMapping != null) {
            methodPath = deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "";
            httpMethod = "DELETE";
        } else if (requestMapping != null) {
            methodPath = requestMapping.value().length > 0 ? requestMapping.value()[0] : "";
            httpMethod = "REQUEST";
        }

        path.append(methodPath);
        return httpMethod + " " + path.toString();
    }

    private String buildResultDescription(Object result) {
        if (result == null) {
            return "返回: null";
        }

        if (result instanceof org.springframework.http.ResponseEntity) {
            org.springframework.http.ResponseEntity<?> response =
                    (org.springframework.http.ResponseEntity<?>) result;
            Object body = response.getBody();

            if (body instanceof java.util.List) {
                return "返回: " + ((java.util.List<?>) body).size() + " 条数据";
            } else if (body instanceof java.util.Map) {
                return "返回: Map 数据";
            } else if (body instanceof byte[]) {
                return "返回: 二进制数据 (" + ((byte[]) body).length + " bytes)";
            } else if (body != null) {
                return "返回: " + body.getClass().getSimpleName();
            }
        }

        if (result instanceof java.util.List) {
            return "返回: " + ((java.util.List<?>) result).size() + " 条数据";
        }

        return "返回: " + result.getClass().getSimpleName();
    }
}