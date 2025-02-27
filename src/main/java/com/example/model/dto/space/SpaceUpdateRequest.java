package com.example.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新空间请求
 *
 * @author WanAn
 */
@Data
public class SpaceUpdateRequest implements Serializable {
    private static final long serialVersionUID = -6665192573001031111L;
    private String spaceName;
    // 空间id
    private Long id;
    // 空间等级 0-普通空间 1-高级空间 2-超级空间
    private Integer spaceLevel;
    private Integer maxSize;
    private Integer maxCount;
}
