package com.example.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.annotation.AuthCheck;
import com.example.common.BaseResponse;
import com.example.common.DeleteRequest;
import com.example.common.ResultUtils;
import com.example.constant.UserConstant;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.model.dto.space.*;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.enums.SpaceLevelEnum;
import com.example.model.vo.SpaceVO;
import com.example.service.SpaceService;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 空间文件Controller
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加信息
     * @param request         request
     * @return 空间id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceAddRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        Long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 空间删除信息
     * @param request       request
     * @return 删除结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(deleteRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 校验只能自己删或者管理员删
        User loginUser = userService.getLoginUser(request);
        Space space = spaceService.getById(deleteRequest.getId());
        if (!loginUser.getId().equals(space.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = spaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除失败");
        return ResultUtils.success(result);
    }

    /**
     * 更新空间
     *
     * @param spaceUpdateRequest 空间更新信息
     * @param request            request
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceUpdateRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);
        // 填充字段
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);
        // 校验是否存在
        Space oldSpace = spaceService.getById(space.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldSpace), ErrorCode.NOT_FOUND, "数据不存在");
        space.setEditTime(new Date());
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取空间
     *
     * @param id 空间id
     * @return 空间信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Space> getSpaceById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(space), ErrorCode.NOT_FOUND, "数据不存在");
        return ResultUtils.success(space);
    }

    /**
     * 根据id获取空间VO
     *
     * @param id 空间id
     * @return 空间信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(space), ErrorCode.NOT_FOUND, "数据不存在");
        SpaceVO spaceVO = spaceService.getSpaceVO(space);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取空间
     *
     * @param spaceQueryRequest 空间查询信息
     * @return 空间信息
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceQueryRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Page<Space> page = spaceService.page(new Page<>(spaceQueryRequest.getCurrentPage(), spaceQueryRequest.getPageSize()),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 分页获取空间VO
     *
     * @param spaceQueryRequest 空间查询信息
     * @return 空间信息
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceQueryRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 限制爬虫
        ThrowUtils.throwIf(spaceQueryRequest.getPageSize() > 50, ErrorCode.PARAM_ERROR, "参数过大");
        Page<SpaceVO> spaceVoPage = spaceService.listSpaceVOByPage(spaceQueryRequest);
        return ResultUtils.success(spaceVoPage);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(spaceEditRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);
        space.setEditTime(new Date());
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space, false);
        // 校验是否存在
        Space oldSpace = spaceService.getById(space.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldSpace), ErrorCode.NOT_FOUND, "数据不存在");
        // 只有本人和管理员可以修改
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTH);
        // 修改
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(result);
    }

    /**
     * 获取空间等级
     *
     * @return 空间等级列表
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue()
                        , spaceLevelEnum.getText()
                        , spaceLevelEnum.getMaxCount()
                        , spaceLevelEnum.getMaxSize())
                ).collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }
}