package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.model.dto.picture.PictureQueryRequest;
import com.example.model.dto.space.SpaceAddRequest;
import com.example.model.dto.space.SpaceQueryRequest;
import com.example.model.entity.Picture;
import com.example.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.entity.User;
import com.example.model.vo.SpaceVO;

/**
 * 空间Service
 *
 * @author WanAn
 */
public interface SpaceService extends IService<Space> {

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求体
     * @param loginUser 登录用户
     * @return 空间ID
     */
    Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间
     *
     * @param space 空间
     * @param add 是否为添加操作
     */
    void validSpace(Space space,boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceQueryRequest 空间查询请求体
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间返回信息
     *
     * @param space 空间
     * @return 空间返回信息
     */
    SpaceVO getSpaceVO(Space space);

    /**
     * 根据空间等级填充空间信息
     *
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 获取空间分页信息
     *
     * @param spacePage 空间分页信息
     * @return 空间分页信息
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage);

    /**
     * 获取空间分页信息（通过分页请求体）
     *
     * @param spaceQueryRequest 空间分页请求体
     * @return 空间分页信息
     */
    Page<SpaceVO> listSpaceVOByPage(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取空间分页信息（通过分页信息）
     *
     * @param spacePage 空间分页信息
     * @return 空间分页信息
     */
    Page<SpaceVO> getSpaceVoPage(Page<Space> spacePage);

    /**
     * 校验空间权限
     *
     * @param loginUser 登录用户
     * @param space 空间
     */
    void checkSpaceAuth(User loginUser, Space space);
}
