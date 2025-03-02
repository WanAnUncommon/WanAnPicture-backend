package com.example.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.mapper.SpaceMapper;
import com.example.model.dto.space.analyze.*;
import com.example.model.entity.Picture;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.vo.*;
import com.example.service.PictureService;
import com.example.service.SpaceAnalyzeService;
import com.example.service.SpaceService;
import com.example.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 空间分析服务实现类
 *
 * @author WanAn
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private PictureService pictureService;

    /**
     * 检查空间分析权限
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param loginUser           登录用户
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        Boolean queryPublic = spaceAnalyzeRequest.getQueryPublic();
        Boolean queryAll = spaceAnalyzeRequest.getQueryAll();
        if (queryAll || queryPublic) {
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH);
        } else {
            // 空间是否存在
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAM_ERROR, "空间id不能为空");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.PARAM_ERROR, "空间不存在");
            // 仅本人或管理员
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 填充查询条件
     *
     * @param spaceAnalyzeRequest 空间分析请求
     * @param queryWrapper        查询包装器
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        Boolean queryPublic = spaceAnalyzeRequest.getQueryPublic();
        Boolean queryAll = spaceAnalyzeRequest.getQueryAll();
        // 查询全空间
        if (queryAll) {
            return;
        }
        // 公共图库
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        // 查询指定空间
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "预料外的查询范围");
    }

    @Override
    public SpaceAnalyzeUsageResponse getSpaceAnalyzeUsage(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 公共空间和全部空间
        if (spaceAnalyzeRequest.getQueryPublic() || spaceAnalyzeRequest.getQueryAll()) {
            // 权限校验
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            // 数据查询
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceAnalyzeRequest, queryWrapper);
            List<Object> objectList = pictureService.getBaseMapper().selectObjs(queryWrapper);
            // 统计数据
            long usedSize = objectList.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = objectList.size();
            // 封装返回结果
            SpaceAnalyzeUsageResponse spaceAnalyzeUsageResponse = new SpaceAnalyzeUsageResponse();
            spaceAnalyzeUsageResponse.setUsedSize(usedSize);
            spaceAnalyzeUsageResponse.setMaxSize(null);
            spaceAnalyzeUsageResponse.setSizeUsageRatio(null);
            spaceAnalyzeUsageResponse.setUsedCount(usedCount);
            spaceAnalyzeUsageResponse.setMaxCount(null);
            spaceAnalyzeUsageResponse.setCountUsageRatio(null);
            return spaceAnalyzeUsageResponse;
        } else { // 查询指定空间
            // 权限校验
            checkSpaceAnalyzeAuth(spaceAnalyzeRequest, loginUser);
            // 数据查询
            Space space = spaceService.getById(spaceAnalyzeRequest.getSpaceId());
            // 统计数据
            long usedSize = space.getTotalSize();
            long usedCount = space.getTotalCount();
            long maxSize = space.getMaxSize();
            long maxCount = space.getMaxCount();
            double sizeUsageRatio = NumberUtil.round(usedSize * 100.0 / maxSize, 2).doubleValue();
            double countUsageRatio = NumberUtil.round(usedCount * 100.0 / maxCount, 2).doubleValue();
            // 封装返回结果
            SpaceAnalyzeUsageResponse spaceAnalyzeUsageResponse = new SpaceAnalyzeUsageResponse();
            spaceAnalyzeUsageResponse.setUsedSize(usedSize);
            spaceAnalyzeUsageResponse.setMaxSize(maxSize);
            spaceAnalyzeUsageResponse.setSizeUsageRatio(sizeUsageRatio);
            spaceAnalyzeUsageResponse.setUsedCount(usedCount);
            spaceAnalyzeUsageResponse.setMaxCount(maxCount);
            spaceAnalyzeUsageResponse.setCountUsageRatio(countUsageRatio);
            return spaceAnalyzeUsageResponse;
        }
    }

    @Override
    public List<SpaceAnalyzeCategoryResponse> getSpaceAnalyzeCategory(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceCategoryAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        queryWrapper.select("category", "count(id) as count", "sum(picSize) as totalSize");
        queryWrapper.groupBy("category");
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String category = (String) result.get("category");
            Long count = (Long) result.get("count");
            Long totalSize = (Long) result.get("totalSize");
            return new SpaceAnalyzeCategoryResponse(category, count, totalSize);
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceAnalyzeTagResponse> getSpaceAnalyzeTag(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceTagAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);
        queryWrapper.select("tag");
        // 查询所有符合条件的tag
        List<String> tagJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().filter(ObjectUtil::isNotNull)
                .map(Object::toString).collect(Collectors.toList());
        // 解析tag并统计
        Map<String, Long> tagCountMap = tagJsonList.stream().flatMap(tagJson -> JSONUtil.toList(tagJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));
        // 封装返回结果，按照次数升序
        return tagCountMap.entrySet().stream().sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> {
                    String tag = entry.getKey();
                    Long count = entry.getValue();
                    return new SpaceAnalyzeTagResponse(tag, count);
                }).collect(Collectors.toList());
    }

    @Override
    public List<SpaceAnalyzeSizeResponse> getSpaceAnalyzeSize(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceSizeAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);
        queryWrapper.select("picSize");
        // 查询所有picSize
        List<Long> picSizeList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().filter(ObjectUtil::isNotNull)
                .map(size -> (Long) size).collect(Collectors.toList());
        // 定义图片大小范围
        Map<String, Long> sizeRange = new LinkedHashMap<>();
        sizeRange.put("<100KB", picSizeList.stream().filter(size -> size < 100 * 1024).count());
        sizeRange.put("100KB~500MB", picSizeList.stream().filter(size -> size >= 100 * 1024 && size <= 500 * 1024).count());
        sizeRange.put(">1MB", picSizeList.stream().filter(size -> size > 1024 * 1024).count());
        // 封装返回结果
        return sizeRange.entrySet().stream()
                .map(entry ->
                        new SpaceAnalyzeSizeResponse(entry.getKey(), entry.getValue())
                ).collect(Collectors.toList());
    }

    @Override
    public List<SpaceAnalyzeUserResponse> getSpaceAnalyzeUser(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(spaceUserAnalyzeRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(userId != null, "userId", userId);
        // 时间维度
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("date_format(createTime,'%Y-%m-%d') as period", "count(id) as count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) as period", "count(id) as count");
                break;
            case "month":
                queryWrapper.select("date_format(createTime,'%Y-%m') as period", "count(id) as count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAM_ERROR, "时间维度错误");
        }
        // 排序
        queryWrapper.groupBy("period").orderByAsc("period");
        // 封装返回结果
        return pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String period = (String) result.get("period");
            Long count = (Long) result.get("count");
            return new SpaceAnalyzeUserResponse(period, count);
        }).collect(Collectors.toList());
    }
}
