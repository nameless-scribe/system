package com.system.config;

import com.system.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理：
 * - 将业务抛出的 IllegalArgumentException / RuntimeException 统一包装为 Result.fail(message)
 * - HTTP 状态码保持 200，前端通过 code/message 做提示
 * - 不会影响拦截器里直接写出的 401/403 响应
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务参数/状态校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务参数异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * 其它未捕获运行时异常，统一兜底
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("系统运行时异常: {}", e.getMessage(), e);
        return Result.fail("系统开小差了，请稍后重试");
    }
}

