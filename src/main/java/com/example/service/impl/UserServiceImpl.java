package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constant.UserConstant;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.mapper.UserMapper;
import com.example.model.dto.user.UserQueryDTO;
import com.example.model.entity.User;
import com.example.model.enums.UserRoleEnum;
import com.example.model.vo.LoginUserVO;
import com.example.model.vo.UserVO;
import com.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lenovo
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-02-07 13:47:20
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 参数校验
        ThrowUtils.throwIf(CharSequenceUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAM_ERROR, "账号/密码为空");
        ThrowUtils.throwIf(userPassword.length() < 8, ErrorCode.PARAM_ERROR, "密码小于8位");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAM_ERROR, "两次输入的密码不同");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAM_ERROR, "账号小于4位");
        // 账号唯一校验
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAM_ERROR, "账号已存在");
        // 保存
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(userPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserPassword(getEncryptPassword(userPassword));
        boolean saved = this.save(user);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "数据库异常");
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐
        final String salt = "Uncommon";
        return DigestUtils.md5DigestAsHex((userPassword + salt).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        ThrowUtils.throwIf(CharSequenceUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAM_ERROR, "账号/密码为空");
        User user = this.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
        ThrowUtils.throwIf(user == null, ErrorCode.PARAM_ERROR, "账号不存在");
        ThrowUtils.throwIf(!user.getUserPassword().equals(getEncryptPassword(userPassword)), ErrorCode.PARAM_ERROR, "密码错误");
        // 脱敏
        LoginUserVO loginUserVO = getLoginUserVO(user);
        // 保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        LoginUserVO loginUserVO = (LoginUserVO) attribute;
        ThrowUtils.throwIf(loginUserVO == null,ErrorCode.NOT_LOGIN);
        User user = this.getById(loginUserVO.getId());
        ThrowUtils.throwIf(user == null,ErrorCode.NOT_LOGIN);
        return user;
    }

    @Override
    public long userLogout(HttpServletRequest request) {
        // 判断是否登录
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        LoginUserVO loginUserVO = (LoginUserVO) attribute;
        ThrowUtils.throwIf(loginUserVO == null,ErrorCode.NOT_LOGIN);
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return loginUserVO.getId();
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryDTO userQueryDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Long id = userQueryDTO.getId();
        String userAccount = userQueryDTO.getUserAccount();
        String userName = userQueryDTO.getUserName();
        String userProfile = userQueryDTO.getUserProfile();
        String userRole = userQueryDTO.getUserRole();
        String sortField = userQueryDTO.getSortField();
        String sortOrder = userQueryDTO.getSortOrder();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(CharSequenceUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(CharSequenceUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(CharSequenceUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.eq(CharSequenceUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.orderBy(CharSequenceUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public long addUser(User user) {
        ThrowUtils.throwIf(user == null,ErrorCode.PARAM_ERROR,"参数为空");
        // 参数校验
        ThrowUtils.throwIf(CharSequenceUtil.hasBlank(user.getUserAccount(), user.getUserPassword(),user.getUserName()), ErrorCode.PARAM_ERROR, "参数为空");
        ThrowUtils.throwIf(user.getUserPassword().length() < 8, ErrorCode.PARAM_ERROR, "密码小于8位");
        ThrowUtils.throwIf(user.getUserAccount().length() < 4, ErrorCode.PARAM_ERROR, "账号小于4位");
        // 账号唯一校验
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",user.getUserAccount());
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAM_ERROR, "账号已存在");
        boolean saved = this.save(user);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "数据库异常");
        return user.getId();
    }

    @Override
    public long updateUser(User user) {
        ThrowUtils.throwIf(user == null || user.getId() == null,ErrorCode.PARAM_ERROR,"参数为空");
        // 参数校验
        ThrowUtils.throwIf(user.getUserPassword()!=null&&user.getUserPassword().length() < 8, ErrorCode.PARAM_ERROR, "密码小于8位");
        ThrowUtils.throwIf(user.getUserAccount()!=null&&user.getUserAccount().length() < 4, ErrorCode.PARAM_ERROR, "账号小于4位");
        // 账号唯一校验
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",user.getUserAccount());
        long count = this.count(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAM_ERROR, "账号已存在");
        boolean result = this.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
        return user.getId();
    }

    @Override
    public List<UserVO> queryUserByPage(UserQueryDTO userQueryDTO) {
        QueryWrapper<User> queryWrapper = getQueryWrapper(userQueryDTO);
        Page<User> page = this.page(new Page<>(userQueryDTO.getCurrentPage(), userQueryDTO.getPageSize()), queryWrapper);
        List<User> records = page.getRecords();
        List<UserVO> userVOList = getUserVOList(records);
        Page<UserVO> userVOPage =new Page<>(userQueryDTO.getCurrentPage(),userQueryDTO.getPageSize(), page.getTotal());
        userVOPage.setRecords(userVOList);
        return userVOPage.getRecords();
    }
}




