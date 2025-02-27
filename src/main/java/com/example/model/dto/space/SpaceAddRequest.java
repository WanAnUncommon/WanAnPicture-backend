package com.example.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建空间请求
 *
 * @author WanAn
 */
@Data
public class SpaceAddRequest implements Serializable {
    private static final long serialVersionUID = -7317152818111409048L;
    private String spaceName;
    private Integer spaceLevel;
}
