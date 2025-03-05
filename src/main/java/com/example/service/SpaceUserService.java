package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.model.dto.space.SpaceAddRequest;
import com.example.model.dto.space.SpaceQueryRequest;
import com.example.model.dto.spaceuser.SpaceUserAddRequest;
import com.example.model.dto.spaceuser.SpaceUserQueryRequest;
import com.example.model.entity.Space;
import com.example.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.entity.User;
import com.example.model.vo.SpaceUserVO;
import com.example.model.vo.SpaceVO;

import java.util.List;

/**
 * 空间用户服务接口
 *
* @author WanAn
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest 空间用户添加请求体
     * @return 空间用户ID
     */
    Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间用户
     *
     * @param spaceUser 空间用户
     * @param add 是否为添加操作
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryRequest 空间用户查询请求体
     * @return 查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户返回信息列表
     *
     * @param spaceUserList 空间用户列表
     * @return 空间用户返回信息列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
