package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.model.dto.user.UserQueryDTO;
import com.example.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.vo.LoginUserVO;
import com.example.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lenovo
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-02-07 13:47:20
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 检查密码
     * @return 用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword);

    /**
     * 加密
     * @param userPassword 密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     *
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request 请求
     * @return 登录用户的脱敏信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏登录用户信息
     *
     * @param user 用户
     * @return 脱敏用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏用户信息
     *
     * @param user 用户
     * @return 脱敏用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取脱敏用户信息
     *
     * @param userList 用户
     * @return 脱敏用户信息
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户退出登录
     *
     * @param request 请求
     * @return 用户id
     */
    long userLogout(HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userQueryDTO 用户
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO);

    /**
     * 添加用户
     *
     * @param user 用户
     * @return 用户id
     */
    long addUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户
     * @return 用户id
     */
    long updateUser(User user);

    /**
     * 查询用户
     *
     * @param userQueryDTO 用户
     * @return 脱敏用户列表
     */
    List<UserVO> queryUserByPage(UserQueryDTO userQueryDTO);
}
