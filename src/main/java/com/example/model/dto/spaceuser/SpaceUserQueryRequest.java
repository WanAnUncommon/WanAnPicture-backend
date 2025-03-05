package com.example.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间成员查询请求
 *
 * @author WanAn
 */
@Data
public class SpaceUserQueryRequest implements Serializable {
    private static final long serialVersionUID = -2555188459933600122L;

    // 空间用户id
    private Long id;

    // 空间id
    private Long spaceId;

    // 用户id
    private Long userId;

    // 空间用户角色 viewer,editor,admin
    private String spaceRole;
}
