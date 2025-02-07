package com.example.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录DTO
 *
 * @author WanAn
 */
@Data
public class UserLoginDTO implements Serializable {
    private static final long serialVersionUID = -3894484558683008633L;
    private String userAccount;
    private String userPassword;
}
