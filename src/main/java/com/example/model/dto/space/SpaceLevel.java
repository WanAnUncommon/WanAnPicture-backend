package com.example.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 空间等级
 *
 * @author WanAn
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    private int value;
    private String text;
    // 空间等级对应的最大数量
    private long maxCount;
    // 空间等级对应的最大空间大小
    private long maxSize;
}
