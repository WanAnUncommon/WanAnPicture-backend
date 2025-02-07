package com.example.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户添加DTO
 *
 * @author WanAn
 */
@Data
public class UserAddDTO implements Serializable {
    private static final long serialVersionUID = -53170363555389071L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;

}
