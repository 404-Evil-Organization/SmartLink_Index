package com.zhilian.zhilianbackend.handler;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:50
 * @Param:
 * @Return:
 * @Description: 全局异常处理器，统一处理业务异常和系统异常
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:15
     * @Param: e 业务异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理业务异常（BusinessException），返回对应的错误码和错误信息
    **/
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode() != null ? e.getCode() : 500, e.getMessage());
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:15
     * @Param: e 系统异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理系统异常（Exception），返回500错误和异常信息（开发阶段便于调试）
    **/
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 开发阶段返回具体错误信息，方便调试
        return Result.error("系统异常: " + e.getMessage());
    }
}
