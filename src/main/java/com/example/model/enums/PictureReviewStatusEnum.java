package com.example.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 图片审核状态枚举类
 *
 * @author WanAn
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("不通过", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取枚举类
     *
     * @param value 值
     * @return 枚举类
     */
    public static PictureReviewStatusEnum getEnumByValue(int value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum :
                PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}
