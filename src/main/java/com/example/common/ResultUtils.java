package com.example.common;

import com.example.exception.ErrorCode;

/**
 * 全局统一返回工具类
 *
 * @author WanAn
 */
public class ResultUtils {
    /**
     * 成功
     *
     * @param data 数据
     * @return 统一返回体
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200,"成功", data);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @return 统一返回体
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message,null);
    }

    /**
     * 失败
     *
     * @param code 错误码
     * @param message   错误信息
     * @return 统一返回体
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, message,null);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @return 统一返回体
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }
}
