package com.example.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.BaseResponse;
import com.example.common.DeleteRequest;
import com.example.common.ResultUtils;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.model.dto.spaceuser.SpaceUserAddRequest;
import com.example.model.dto.spaceuser.SpaceUserEditRequest;
import com.example.model.dto.spaceuser.SpaceUserQueryRequest;
import com.example.model.entity.SpaceUser;
import com.example.model.entity.User;
import com.example.model.vo.SpaceUserVO;
import com.example.service.SpaceUserService;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 空间用户文件Controller
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private UserService userService;

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest 空间用户添加信息
     * @return 空间用户id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUserAddRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Long spaceUserId = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(spaceUserId);
    }

    /**
     * 删除空间用户
     *
     * @param deleteRequest 空间用户删除信息
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(deleteRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 数据是否存在
        SpaceUser spaceUser = spaceUserService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUser), ErrorCode.NOT_FOUND, "数据不存在");
        boolean result = spaceUserService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除失败");
        return ResultUtils.success(result);
    }

    /**
     * 获取空间用户信息
     *
     * @param spaceUserQueryRequest 空间用户查询信息
     * @return 空间用户信息
     */
    @PostMapping("/get")
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUserQueryRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Long userId = spaceUserQueryRequest.getUserId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(userId, spaceId), ErrorCode.PARAM_ERROR, "参数为空");
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUser), ErrorCode.NOT_FOUND, "数据不存在");
        return ResultUtils.success(spaceUser);
    }

    /**
     * 获取空间用户列表
     *
     * @param spaceUserQueryRequest 空间用户查询信息
     * @return 空间用户列表
     */
    @PostMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceUserVOList(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUserQueryRequest), ErrorCode.PARAM_ERROR, "参数为空");
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtils.success(spaceUserVOList);
    }

    /**
     * 获取当前用户团队空间列表
     *
     * @param request 请求
     * @return 空间用户列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtils.success(spaceUserVOList);
    }

    /**
     * 更新空间用户
     *
     * @param spaceUserEditRequest 空间用户修改信息
     * @return 更新结果
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUserEditRequest), ErrorCode.PARAM_ERROR, "参数为空");
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest, spaceUser);
        spaceUserService.validSpaceUser(spaceUser, false);
        // 校验是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(spaceUser.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldSpaceUser), ErrorCode.NOT_FOUND, "数据不存在");
        // 修改
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(result);
    }
}