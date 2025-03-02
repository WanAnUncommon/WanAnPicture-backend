package com.example.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间分析请求
 *
 * @author WanAn
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {
    // 空间ID
    private Long spaceId;

    // 是否为公共空间
    private Boolean queryPublic;

    // 是否查询全部空间
    private Boolean queryAll;

    private static final long serialVersionUID = -698927907586151039L;
}
