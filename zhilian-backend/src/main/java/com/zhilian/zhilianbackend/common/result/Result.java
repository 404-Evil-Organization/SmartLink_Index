package com.zhilian.zhilianbackend.common.result;

import lombok.Data;

@Data

/**
 * @Author: 6017
 * @Date: 2026/3/9 21:21
 * @Param: 
 * @Return: 
 * @Description: 统一响应结果封装类，所有接口返回的标准格式
**/
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /**
     * @Author: 6017
     * @Date: 2026/3/9 21:22
     * @Param: code 状态码
     * @Return: message 提示信息
     * @Description: 私有构造方法，防止外部直接创建
    **/
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    // 错误响应 - 对应文档中的各种状态码
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }

    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }
}