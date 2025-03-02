package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.dto.space.analyze.*;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.vo.*;

import java.util.List;

/**
 * 空间分析Service
 *
 * @author WanAn
 */
public interface SpaceAnalyzeService extends IService<Space> {
    /**
     * 获取空间使用情况
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param loginUser           登录用户
     * @return 空间使用情况
     */
    SpaceAnalyzeUsageResponse getSpaceAnalyzeUsage(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser);

    /**
     * 获取空间分类情况
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求
     * @param loginUser                   登录用户
     * @return 空间分类情况
     */
    List<SpaceAnalyzeCategoryResponse> getSpaceAnalyzeCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间标签情况
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求
     * @param loginUser              登录用户
     * @return 空间标签情况
     */
    List<SpaceAnalyzeTagResponse> getSpaceAnalyzeTag(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间大小情况
     *
     * @param spaceSizeAnalyzeRequest 空间大小分析请求
     * @param loginUser               登录用户
     * @return 空间大小情况
     */
    List<SpaceAnalyzeSizeResponse> getSpaceAnalyzeSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户情况
     *
     * @param spaceUserAnalyzeRequest 空间用户分析请求
     * @param loginUser               登录用户
     * @return 空间用户情况
     */
    List<SpaceAnalyzeUserResponse> getSpaceAnalyzeUser(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);
}
