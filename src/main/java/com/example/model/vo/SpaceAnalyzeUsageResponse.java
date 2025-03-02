package com.example.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间资源使用情况分析返回类
 *
 * @author WanAn
 */
@Data
public class SpaceAnalyzeUsageResponse implements Serializable {
    // 已用大小
    private Long usedSize;

    // 总大小
    private Long maxSize;

    // 大小使用比率
    private Double sizeUsageRatio;

    // 当前图片数量
    private Long usedCount;

    // 总图片数量
    private Long maxCount;

    // 数量使用比率
    private Double countUsageRatio;

    private static final long serialVersionUID = -5443791157195278850L;
}