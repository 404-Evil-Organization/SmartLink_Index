package com.zhilian.zhilianbackend.handler;

import com.zhilian.zhilianbackend.common.result.Result;
import com.zhilian.zhilianbackend.exception.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:50
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
     * @Date: 2026/3/11 16:30
     * @Param: e 参数校验异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理@Valid参数校验异常，返回400错误和具体的字段错误信息
     **/
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验异常: {}", message);
        return Result.badRequest(message);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:30
     * @Param: e 参数绑定异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理参数绑定异常，返回400错误
     **/
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("参数绑定异常: {}", message);
        return Result.badRequest(message);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:30
     * @Param: e 单个参数校验异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理单个参数校验异常（如@RequestParam上的校验），返回400错误
     **/
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.error("参数校验异常: {}", message);
        return Result.badRequest(message);
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:30
     * @Param: e JWT异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理JWT解析异常，返回401未授权错误
     **/
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwtException(JwtException e) {
        log.error("JWT异常: {}", e.getMessage());
        return Result.unauthorized("无效的Token");
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:30
     * @Param: e JWT过期异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理JWT过期异常，返回401未授权错误
     **/
    @ExceptionHandler(ExpiredJwtException.class)
    public Result<Void> handleExpiredJwtException(ExpiredJwtException e) {
        log.error("JWT过期: {}", e.getMessage());
        return Result.unauthorized("Token已过期");
    }

    /**
     * @Author: 6017
     * @Date: 2026/3/11 16:30
     * @Param: e 系统异常对象
     * @Return: Result<Void> 统一错误响应
     * @Description: 处理所有未捕获的异常，返回通用错误信息，避免信息泄露
     **/
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        // 返回通用错误信息，不暴露内部细节
        return Result.error("系统繁忙，请稍后重试");
    }
}