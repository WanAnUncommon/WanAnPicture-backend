package com.example.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间使用排行分析请求
 *
 * @author WanAn
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {
    // 空间使用排行前N个
    private Integer topN=10;

    private static final long serialVersionUID = 5949658015999238786L;
}
