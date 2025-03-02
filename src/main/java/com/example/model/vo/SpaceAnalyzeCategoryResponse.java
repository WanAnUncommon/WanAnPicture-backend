package com.example.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间分类情况分析返回类
 *
 * @author WanAn
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceAnalyzeCategoryResponse implements Serializable {
    // 分类
    private String category;

    // 数量
    private Long count;

    // 总大小
    private Long totalSize;

    private static final long serialVersionUID = 3828105498493332964L;
}