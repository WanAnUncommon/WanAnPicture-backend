package com.example.exception;

/**
 * 异常处理类
 *
 * @author WanAn
 */
public class ThrowUtils {
    /**
     * 条件condition为true时，抛出runtimeException
     *
     * @param condition        条件
     * @param runtimeException 要抛的RuntimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件condition为true时，抛出businessException
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件condition为true时，抛出businessException
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
