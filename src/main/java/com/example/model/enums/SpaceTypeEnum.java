package com.example.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 空间类型枚举类
 *
 * @author WanAn
 */
@Getter
public enum SpaceTypeEnum {
    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);

    private final String text;
    private final int value;

    /**
     * 构造方法
     *
     * @param text  文本
     * @param value 值
     */
    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取枚举类
     *
     * @param value 值
     * @return 枚举类
     */
    public static SpaceTypeEnum getEnumByValue(int value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum :
                SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value == value) {
                return spaceTypeEnum;
            }
        }
        return null;
    }
}
