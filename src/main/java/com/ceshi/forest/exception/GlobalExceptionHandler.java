package com.ceshi.forest.exception;

import com.ceshi.forest.dto.ResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResultDTO<Void> handleBadCredentials(BadCredentialsException e) {
        return ResultDTO.fail(401, "用户名或密码错误");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResultDTO<Void> handleAccessDenied(AccessDeniedException e) {
        return ResultDTO.fail(403, "没有权限访问该资源");
    }

    @ExceptionHandler(BindException.class)
    public ResultDTO<Void> handleValidation(BindException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResultDTO.fail(400, message);
    }

    /**
     * 静默处理静态资源找不到的错误（favicon.ico、Chrome DevTools 等）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT) // 返回 204，不记录日志
    public void handleNoResourceFound(NoResourceFoundException e) {
        // 完全静默处理，不记录任何日志
    }

    @ExceptionHandler(RuntimeException.class)
    public ResultDTO<Void> handleRuntime(RuntimeException e) {
        log.error("运行时异常: {}", e.getMessage(), e);
        return ResultDTO.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResultDTO<Void> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResultDTO.fail("系统繁忙，请稍后重试");
    }
}