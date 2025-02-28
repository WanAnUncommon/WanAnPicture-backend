package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.mapper.SpaceMapper;
import com.example.model.dto.space.SpaceAddRequest;
import com.example.model.dto.space.SpaceQueryRequest;
import com.example.model.entity.Picture;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.enums.SpaceLevelEnum;
import com.example.model.vo.PictureVO;
import com.example.model.vo.SpaceVO;
import com.example.service.SpaceService;
import com.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author WanAn
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAM_ERROR, "参数为空");
        // 填充默认值
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        if (space.getSpaceName() == null) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        this.fillSpaceBySpaceLevel(space);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        // 权限校验
        if (space.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 校验
        this.validSpace(space, true);
        // 用户只能有一个私有空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            return transactionTemplate.execute(status -> {
                // 判断是否已经有私有空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.PARAM_ERROR, "已有私有空间");
                // 插入数据
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "数据库异常");
                return space.getId();
            });
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        if (add) {
            ThrowUtils.throwIf(CharSequenceUtil.isBlank(spaceName), ErrorCode.PARAM_ERROR, "空间名称为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAM_ERROR, "空间等级为空");
        }
        ThrowUtils.throwIf(spaceName.length() > 30, ErrorCode.PARAM_ERROR, "空间名称长度不能超过30");
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAM_ERROR, "空间等级错误");
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        String spaceName = spaceQueryRequest.getSpaceName();
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        // 拼装查询条件
        queryWrapper.like(CharSequenceUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.eq(spaceLevel != null, "spaceLevel", spaceLevel);
        queryWrapper.orderBy(CharSequenceUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            spaceVO.setUserVO(userService.getUserVO(user));
        }
        return spaceVO;
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        ThrowUtils.throwIf(spaceLevelEnum == null, ErrorCode.PARAM_ERROR, "空间等级错误");
        if (space.getMaxSize() == null) {
            space.setMaxSize(spaceLevelEnum.getMaxSize());
        }
        if (space.getMaxCount() == null) {
            space.setMaxCount(spaceLevelEnum.getMaxCount());
        }
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage) {
        List<Space> spaceRecords = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceRecords)) {
            return spaceVOPage;
        }
        List<SpaceVO> spaceVOList = spaceRecords.stream().map(this::getSpaceVO).collect(Collectors.toList());
        // 获取userId列表
        List<Long> userIdList = spaceRecords.stream().map(Space::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIdList);
        // 根据id封装成map
        Map<Long, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getId));
        for (SpaceVO spaceVO : spaceVOList) {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            spaceVO.setUserVO(userService.getUserVO(user));
        }
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public Page<SpaceVO> listSpaceVOByPage(SpaceQueryRequest spaceQueryRequest) {
        // 查询数据库
        Page<Space> spacePage = this.page(new Page<>(spaceQueryRequest.getCurrentPage(),
                        spaceQueryRequest.getPageSize()),
                this.getQueryWrapper(spaceQueryRequest));
        return this.getSpaceVoPage(spacePage);
    }

    @Override
    public Page<SpaceVO> getSpaceVoPage(Page<Space> spacePage) {
        List<Space> spaceRecords = spacePage.getRecords();
        Page<SpaceVO> spaceVoPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceRecords)) {
            return spaceVoPage;
        }
        List<SpaceVO> spaceVOList = spaceRecords.stream().map(this::getSpaceVO).collect(Collectors.toList());
        // 获取userId列表
        List<Long> userIdList = spaceRecords.stream().map(Space::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIdList);
        // 根据id封装成map
        Map<Long, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getId));
        for (SpaceVO spaceVO : spaceVOList) {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            spaceVO.setUserVO(userService.getUserVO(user));
        }
        spaceVoPage.setRecords(spaceVOList);
        return spaceVoPage;
    }
}




