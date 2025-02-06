package com.example.exception;

import lombok.Getter;

/**
 * 自定义异常
 *
 * @author WanAn
 */
@Getter
public class BusinessException extends RuntimeException{
    // 错误码
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode code) {
        super(code.getMessage());
        this.code = code.getCode();
    }

    public BusinessException(ErrorCode code,String message) {
        super(message);
        this.code = code.getCode();
    }
}
