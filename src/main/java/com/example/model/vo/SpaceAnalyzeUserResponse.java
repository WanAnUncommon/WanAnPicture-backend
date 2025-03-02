package com.example.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户上传情况分析返回类
 *
 * @author WanAn
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpaceAnalyzeUserResponse implements Serializable {
    // 时间区间
    private String period;

    // 使用数量
    private Long count;

    private static final long serialVersionUID = 1573067020888901981L;
}