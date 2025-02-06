package com.example.exception;

import lombok.Getter;

/**
 * 自定义错误码
 *
 * @author WanAn
 */
@Getter
public enum ErrorCode {
    SUCCESS(200,"成功"),
    PARAM_ERROR(40000,"请求参数错误"),
    NOT_LOGIN(40100,"未登录"),
    NO_AUTH(40101,"无权限"),
    FORBIDDEN(40300,"请求被禁止"),
    NOT_FOUND(40400,"请求资源不存在"),
    SYSTEM_ERROR(50000,"系统内部错误");

    // 错误码
    private final int code;
    // 错误信息
    private final String message;

    ErrorCode(int code, String message) {
        this.code=code;
        this.message=message;
    }
}
