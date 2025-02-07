package com.example.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册DTO
 *
 * @author WanAn
 */
@Data
public class UserRegisterDTO implements Serializable {
    private static final long serialVersionUID = 5305868521303089362L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
