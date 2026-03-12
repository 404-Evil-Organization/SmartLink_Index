package com.zhilian.zhilianbackend.exception;

/**
 * @Author: 周冠杰
 * @Date: 2026/3/10 23:50
 * @Param:
 * @Return:
 * @Description: 自定义业务异常类，用于区分业务错误和系统错误
 **/
public class BusinessException extends RuntimeException {
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
