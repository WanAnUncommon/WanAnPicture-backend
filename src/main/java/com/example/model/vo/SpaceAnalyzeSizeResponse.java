package com.example.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间图片大小情况分析返回类
 *
 * @author WanAn
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceAnalyzeSizeResponse implements Serializable {
    // 大小范围
    private String sizeRange;

    // 数量
    private Long count;

    private static final long serialVersionUID = 9057621263544076263L;
}