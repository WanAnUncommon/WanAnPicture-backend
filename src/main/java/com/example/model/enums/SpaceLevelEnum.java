package com.example.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 空间等级枚举类
 *
 * @author WanAn
 */
@Getter
public enum SpaceLevelEnum {
    COMMON("普通空间", 0,100,100*1024),
    PROFESSIONAL("高级空间", 1,1000,1000*1024),
    FLAGSHIP("超级空间", 2,10000,1000*1024*1024);

    private final String text;
    private final int value;
    private final int maxSize;
    private final int maxCount;

    /**
     * 构造方法
     *
     * @param text      文本
     * @param value     值
     * @param maxCount  最大数量
     * @param maxSize   最大大小
     */
    SpaceLevelEnum(String text, int value, int maxCount, int maxSize) {
        this.text = text;
        this.value = value;
        this.maxSize = maxSize;
        this.maxCount = maxCount;
    }

    /**
     * 根据value值获取枚举类
     *
     * @param value 值
     * @return 枚举类
     */
    public static SpaceLevelEnum getEnumByValue(int value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceLevelEnum spaceLevelEnum :
                SpaceLevelEnum.values()) {
            if (spaceLevelEnum.value == value) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
