package com.example.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户上传情况分析请求
 *
 * @author WanAn
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {
    // 用户ID
    private Long userId;

    // 时间维度 day, week ,month
    private String timeDimension;
}
