package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import com.example.annotation.AuthCheck;
import com.example.common.BaseResponse;
import com.example.common.ResultUtils;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.model.dto.space.analyze.*;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.vo.*;
import com.example.service.SpaceAnalyzeService;
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
 * 空间分析Controller
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/space/analyze")
@Slf4j
public class SpaceAnalyzeController {

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;
    @Resource
    private UserService userService;

    /**
     * 获取空间使用情况
     *
     * @param spaceUsageAnalyzeRequest 请求体
     * @param request                  HttpServletRequest
     * @return 空间使用情况
     */
    @PostMapping("/usage")
    public BaseResponse<SpaceAnalyzeUsageResponse> getSpaceAnalyzeUsage(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceUsageAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        SpaceAnalyzeUsageResponse spaceAnalyzeUsage = spaceAnalyzeService.getSpaceAnalyzeUsage(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceAnalyzeUsage);
    }

    /**
     * 获取空间分类情况
     *
     * @param spaceCategoryAnalyzeRequest 请求体
     * @param request                     HttpServletRequest
     * @return 空间分类情况
     */
    @PostMapping("/category")
    public BaseResponse<List<SpaceAnalyzeCategoryResponse>> getSpaceAnalyzeCategory(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceCategoryAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        List<SpaceAnalyzeCategoryResponse> responseList = spaceAnalyzeService.getSpaceAnalyzeCategory(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(responseList);
    }

    /**
     * 获取空间标签情况
     *
     * @param spaceTagAnalyzeRequest 请求体
     * @param request                HttpServletRequest
     * @return 空间标签情况
     */
    @PostMapping("/tag")
    public BaseResponse<List<SpaceAnalyzeTagResponse>> getSpaceAnalyzeTag(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceTagAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        List<SpaceAnalyzeTagResponse> responseList = spaceAnalyzeService.getSpaceAnalyzeTag(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(responseList);
    }

    /**
     * 获取空间大小情况
     *
     * @param spaceSizeAnalyzeRequest 请求体
     * @param request                 HttpServletRequest
     * @return 空间大小情况
     */
    @PostMapping("/size")
    public BaseResponse<List<SpaceAnalyzeSizeResponse>> getSpaceAnalyzeSize(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceSizeAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        List<SpaceAnalyzeSizeResponse> responseList = spaceAnalyzeService.getSpaceAnalyzeSize(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(responseList);
    }

    /**
     * 获取空间用户情况
     *
     * @param spaceUserAnalyzeRequest 请求体
     * @param request                 HttpServletRequest
     * @return 空间用户情况
     */
    @PostMapping("/user")
    public BaseResponse<List<SpaceAnalyzeUserResponse>> getSpaceAnalyzeUser(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceUserAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        List<SpaceAnalyzeUserResponse> responseList = spaceAnalyzeService.getSpaceAnalyzeUser(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(responseList);
    }

    /**
     * 获取空间排名情况
     *
     * @param spaceRankAnalyzeRequest 请求体
     * @param request                 HttpServletRequest
     * @return 空间排名情况
     */
    @PostMapping("/rank")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<Space>> getSpaceAnalyzeRank(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceRankAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        List<Space> responseList = spaceAnalyzeService.getSpaceAnalyzeRank(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(responseList);
    }
}