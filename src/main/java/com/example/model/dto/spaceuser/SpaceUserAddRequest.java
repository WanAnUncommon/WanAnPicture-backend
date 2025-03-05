package com.example.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间成员添加请求
 *
 * @author WanAn
 */
@Data
public class SpaceUserAddRequest implements Serializable {
    private static final long serialVersionUID = 5300433017068579652L;
    private Long spaceId;

    private Long userId;

    // 空间角色 viewer,editor,admin
    private String spaceRole;
}
