package com.example.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * 空间用户角色枚举类
 *
 * @author WanAn
 */
@Getter
public enum SpaceRoleEnum {
    VIEWER("观察者", "viewer"),
    EDITOR("编辑者", "editor"),
    ADMIN("管理者", "admin");

    private final String text;
    private final String value;

    /**
     * 构造方法
     *
     * @param text  文本
     * @param value 值
     */
    SpaceRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取枚举类
     *
     * @param value 值
     * @return 枚举类
     */
    public static SpaceRoleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceRoleEnum spaceLevelEnum :
                SpaceRoleEnum.values()) {
            if (Objects.equals(spaceLevelEnum.value, value)) {
                return spaceLevelEnum;
            }
        }
        return null;
    }
}
