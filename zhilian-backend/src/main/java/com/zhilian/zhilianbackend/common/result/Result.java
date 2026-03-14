package com.zhilian.zhilianbackend.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author: 6017
 * @Date: 2026/3/9 23:10
 * @Param:
 * @Return:
 * @Description: 统一响应结果封装类，所有接口返回的标准格式
**/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // 为null的字段不返回
public class Result<T> {

    @JsonProperty("code")  // 明确指定JSON字段名
    private Integer code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    /**
     * @Author: 6017
     * @Date: 2026/3/9 23:16
     * @Param: code 状态码
     * @Return: message 提示信息
     * @Description: 私有构造方法，防止外部直接创建
    **/
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ============== 成功响应 ==============

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 成功响应（有数据，默认消息）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（自定义消息，有数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 成功响应（自定义消息，无数据）
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }

    // ============== 错误响应 ==============

    /**
     * 默认服务器错误 (500)
     */
    public static <T> Result<T> error() {
        return new Result<>(500, "服务器内部错误", null);
    }

    /**
     * 自定义错误消息 (500)
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 自定义错误码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 请求参数错误 (400)
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 未授权 (401)
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }

    /**
     * 禁止访问 (403)
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }

    /**
     * 资源不存在 (404)
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }

    /**
     * 请求冲突 (409)
     */
    public static <T> Result<T> conflict(String message) {
        return new Result<>(409, message, null);
    }

    /**
     * 请求过多 (429)
     */
    public static <T> Result<T> tooManyRequests(String message) {
        return new Result<>(429, message, null);
    }

    // ============== 业务状态码方法 ==============

    /**
     * 自定义状态码和消息，带数据
     */
    public static <T> Result<T> of(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }

    /**
     * 判断请求是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}