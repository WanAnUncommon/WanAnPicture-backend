package com.example.common;

import com.example.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一返回类
 *
 * @author WanAn
 */
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this.code = code;
        this.message = "";
        this.data = data;
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
