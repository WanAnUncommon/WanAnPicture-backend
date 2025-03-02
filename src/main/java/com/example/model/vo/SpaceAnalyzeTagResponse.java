package com.example.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间标签情况分析返回类
 *
 * @author WanAn
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceAnalyzeTagResponse implements Serializable {
    // 标签
    private String tag;

    // 使用数量
    private Long count;

    private static final long serialVersionUID = -2878058640706888913L;
}