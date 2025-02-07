package com.example.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.annotation.AuthCheck;
import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.constant.UserConstant;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.model.dto.user.*;
import com.example.model.entity.User;
import com.example.model.vo.LoginUserVO;
import com.example.model.vo.UserVO;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户Controller
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册DTO
     * @return 用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userRegisterDTO), ErrorCode.PARAM_ERROR, "参数为空");
        String userAccount = userRegisterDTO.getUserAccount();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();
        long id = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(id);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录DTO
     * @return 登录用户脱敏信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(userLoginDTO), ErrorCode.PARAM_ERROR, "参数为空");
        String userAccount = userLoginDTO.getUserAccount();
        String userPassword = userLoginDTO.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 登录用户脱敏信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户退出登录
     *
     * @param request 请求
     * @return 用户id
     */
    @PostMapping("/logout")
    public BaseResponse<Long> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 新增用户
     *
     * @param userAddDTO 用户新增DTO
     * @return 用户id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAddDTO userAddDTO) {
        ThrowUtils.throwIf(userAddDTO == null, ErrorCode.PARAM_ERROR, "参数为空");
        User user = new User();
        BeanUtil.copyProperties(userAddDTO, user);
        return ResultUtils.success(userService.addUser(user));
    }

    /**
     * 删除用户
     *
     * @param id 用户id
     * @return 用户id
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Long> deleteUserById(long id) {
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(id);
    }

    /**
     * 更新用户
     *
     * @param userUpdateDTO 用户更新DTO
     * @return 用户id
     */
    @PostMapping("/update")
    public BaseResponse<Long> updateUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        ThrowUtils.throwIf(userUpdateDTO == null, ErrorCode.PARAM_ERROR, "参数为空");
        User user = new User();
        BeanUtil.copyProperties(userUpdateDTO, user);
        return ResultUtils.success(userService.updateUser(user));
    }

    /**
     * 根据id查询用户
     *
     * @param id 用户id
     * @return 用户脱敏信息
     */
    @GetMapping("/query")
    public BaseResponse<UserVO> queryUserById(long id) {
        User user = userService.getById(id);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页查询用户
     *
     * @param userQueryDTO 用户查询DTO
     * @return 用户脱敏信息列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<List<UserVO>> queryUserByPage(@RequestBody UserQueryDTO userQueryDTO) {
        List<UserVO> userVOList = userService.queryUserByPage(userQueryDTO);
        return ResultUtils.success(userVOList);
    }
}
