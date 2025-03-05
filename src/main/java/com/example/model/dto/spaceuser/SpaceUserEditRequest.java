package com.example.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间成员编辑请求
 *
 * @author WanAn
 */
@Data
public class SpaceUserEditRequest implements Serializable {
    private static final long serialVersionUID = 987911198991688445L;

    // 空间用户id
    private Long id;

    // 空间角色 viewer,editor,admin
    private String spaceRole;
}
