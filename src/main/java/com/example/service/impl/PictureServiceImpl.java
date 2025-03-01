package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.DeleteRequest;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.manager.CosManager;
import com.example.manager.upload.FilePictureUpload;
import com.example.manager.upload.PictureUploadTemplate;
import com.example.manager.upload.UrlPictureUpload;
import com.example.mapper.PictureMapper;
import com.example.model.dto.file.UploadPictureResult;
import com.example.model.dto.picture.*;
import com.example.model.entity.Picture;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.enums.PictureReviewStatusEnum;
import com.example.model.vo.PictureVO;
import com.example.service.PictureService;
import com.example.service.SpaceService;
import com.example.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 针对表【picture(图片表)】的数据库操作Service实现
 *
 * @author WanAn
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // 本地缓存
    private final Cache<String, String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)// 初识条数
            .maximumSize(10000L)// 最多条数
            .expireAfterWrite(Duration.ofMinutes(5)) // 写入5分钟后过期
            .build();

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN);
        ThrowUtils.throwIf(ObjectUtil.isNull(pictureUploadRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 空间校验
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isNull(space), ErrorCode.NOT_FOUND, "空间不存在");
            // 仅空间所有人可以上传图片
            ThrowUtils.throwIf(!loginUser.getId().equals(space.getUserId()), ErrorCode.NO_AUTH);
            // 空间限额校验
            ThrowUtils.throwIf(space.getTotalCount() >= space.getMaxCount(), ErrorCode.NO_AUTH, "空间图片数量已达上限");
        }
        // 判断是否是更新
        Long pictureId = pictureUploadRequest.getId();
        // 是更新
        if (pictureId != null) {
            // 判断图片是否存在
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND, "图片不存在");
            // 仅自己或管理员可以更新
            if (!loginUser.getId().equals(oldPicture.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            // spaceId
            if (spaceId == null) {
                spaceId = oldPicture.getSpaceId();
            } else {
                ThrowUtils.throwIf(!spaceId.equals(oldPicture.getSpaceId()), ErrorCode.NO_AUTH, "空间不匹配");
            }
        }
        // 上传图片到公共图库
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片到私有空间
        if (spaceId != null) {
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        // 根据文件输入源选择上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 存入数据库
        Picture picture = new Picture();
        BeanUtil.copyProperties(uploadPictureResult, picture);
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);
        if (CharSequenceUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picture.setName(pictureUploadRequest.getPicName());
        }
        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        // 如果是更新
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        // 事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 保存到数据库
            boolean save = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "上传文件失败，数据库异常");
            // 更新空间额度
            if (finalSpaceId != null) {
                // 更新空间额度
                boolean update = spaceService.lambdaUpdate().eq(Space::getId, finalSpaceId)
                        .setSql("totalSize=totalSize+" + picture.getPicSize())
                        .setSql("totalCount=totalCount+1").update();
                ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "上传文件失败，数据库异常");
            }
            return save;
        });
        // 脱敏
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchTest = pictureQueryRequest.getSearchText();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(CharSequenceUtil.isNotBlank(name), "name", name);
        queryWrapper.like(CharSequenceUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(CharSequenceUtil.isNotBlank(category), "category", category);
        if (tags != null) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(picSize != null, "picSize", picSize);
        queryWrapper.eq(picWidth != null, "picWidth", picWidth);
        queryWrapper.eq(picHeight != null, "picHeight", picHeight);
        queryWrapper.eq(picScale != null, "picScale", picScale);
        queryWrapper.eq(CharSequenceUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(reviewerId != null, "reviewerId", reviewerId);
        queryWrapper.eq(reviewStatus != null, "reviewStatus", reviewStatus);
        queryWrapper.eq(spaceId != null, "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.like(CharSequenceUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        if (searchTest != null) {
            queryWrapper.and(qw -> {
                qw.like("name", searchTest);
                qw.or();
                qw.like("introduction", searchTest);
            });
        }
        queryWrapper.ge(startEditTime != null, "editTime", startEditTime);
        queryWrapper.lt(endEditTime != null, "editTime", endEditTime);
        queryWrapper.orderBy(CharSequenceUtil.isNotBlank(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            pictureVO.setUserVO(userService.getUserVO(user));
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVoPage(Page<Picture> picturePage) {
        List<Picture> pictureRecords = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureRecords)) {
            return pictureVOPage;
        }
        List<PictureVO> pictureVOList = pictureRecords.stream().map(this::getPictureVO).collect(Collectors.toList());
        // 获取userId列表
        List<Long> userIdList = pictureRecords.stream().map(Picture::getUserId).collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIdList);
        // 根据id封装成map
        Map<Long, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getId));
        for (PictureVO pictureVO : pictureVOList) {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
            }
            pictureVO.setUserVO(userService.getUserVO(user));
        }
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        if (picture == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "参数为空");
        }
        ThrowUtils.throwIf(picture.getId() == null, ErrorCode.PARAM_ERROR, "id不能为空");
        ThrowUtils.throwIf(picture.getUrl() != null && picture.getUrl().length() > 1024, ErrorCode.PARAM_ERROR, "url过长");
        ThrowUtils.throwIf(picture.getName() != null && picture.getName().length() > 128, ErrorCode.PARAM_ERROR, "名称过长");
        ThrowUtils.throwIf(picture.getIntroduction() != null && picture.getIntroduction().length() > 1024, ErrorCode.PARAM_ERROR, "简洁过长");
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAM_ERROR, "参数为空");
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        ThrowUtils.throwIf(id == null || reviewStatusEnum == null, ErrorCode.PARAM_ERROR, "参数为空");
        // 不能重复校验，审核状态相同
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND, "图片不存在");
        ThrowUtils.throwIf(oldPicture.getReviewStatus().equals(reviewStatus), ErrorCode.PARAM_ERROR, "审核状态相同,不可重复审核");
        // 操作数据库
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, picture);
        picture.setReviewerId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "数据库异常");
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        // 管理员自动过审
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        Integer count = pictureUploadByBatchRequest.getCount();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        ThrowUtils.throwIf(count == null || count > 30, ErrorCode.PARAM_ERROR, "最多抓取30条");
        // 构建初识名称
        String namePrefix = searchText;
        if (CharSequenceUtil.isNotBlank(pictureUploadByBatchRequest.getNamePrefix())) {
            namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        }
        // 抓取文档
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("抓取文档失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "抓取文档失败");
        }
        // 解析内容
        Element element = document.getElementsByClass("dgControl").first();
        ThrowUtils.throwIf(ObjectUtil.isEmpty(element), ErrorCode.SYSTEM_ERROR, "解析内容失败");
        Elements imgElementList = element.select("img.mimg");
        // 遍历元素，处理并上传
        int countUpload = 0;
        for (Element imgElement : imgElementList) {
            String url = imgElement.attr("src");
            if (CharSequenceUtil.isBlank(url)) {
                log.info("当前链接为空，已跳过：{}", url);
                continue;
            }
            // 截取有效链接
            int index = url.indexOf("?");
            if (index > -1) {
                url = url.substring(0, index);
            }
            // 构建上传请求体
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(url);
            pictureUploadRequest.setPicName(namePrefix + countUpload);
            // 上传图片
            try {
                this.uploadPicture(url, pictureUploadRequest, loginUser);
                countUpload++;
            } catch (Exception e) {
                log.error("上传图片失败", e);
                continue;
            }
            if (countUpload >= count) {
                break;
            }
        }
        return countUpload;
    }

    @Override
    public Page<PictureVO> listPictureVoByPage(PictureQueryRequest pictureQueryRequest) {
        /*// 先查询缓存
        // 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(String.format("WanAnPicture:listPictureVOByPage:%s", queryCondition).getBytes());
        // 先查本地缓存
        String cacheValue = LOCAL_CACHE.getIfPresent(hashKey);
        // 如果缓存命中
        if (cacheValue != null) {
            return JSONUtil.toBean(cacheValue, Page.class);
        }
        // 本地缓存未命中，查询Redis缓存
        ValueOperations<String, String> cacheMap = stringRedisTemplate.opsForValue();
        cacheValue = cacheMap.get(hashKey);
        // 如果缓存命中
        if (cacheValue != null) {
            // 写入本地缓存
            LOCAL_CACHE.put(hashKey, cacheValue);
            return JSONUtil.toBean(cacheValue, Page.class);
        }*/
        // 缓存都未命中,查询数据库
        Page<Picture> picturePage = this.page(new Page<>(pictureQueryRequest.getCurrentPage(),
                        pictureQueryRequest.getPageSize()),
                this.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = this.getPictureVoPage(picturePage);
        /*// 缓存结果
        // 写入Redis，设置缓存过期时间为5-10分钟，避免缓存雪崩
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        cacheMap.set(hashKey, JSONUtil.toJsonStr(pictureVOPage), expireTime, TimeUnit.SECONDS);
        // 写入本地缓存
        LOCAL_CACHE.put(hashKey, JSONUtil.toJsonStr(pictureVOPage));*/
        return pictureVOPage;
    }

    @Async
    @Override
    public void deleteCosPicture(Picture picture) {
        String url = picture.getUrl();
        // 如果被其它图片引用，则不删除图片文件
        Long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        if (count > 1) {
            return;
        }
        // 删除图片文件
        cosManager.deleteObject(url);
        // 删除缩略图
        String thumbnailUrl = picture.getThumbnailUrl();
        if (CharSequenceUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public boolean deletePicture(DeleteRequest deleteRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.NOT_LOGIN);
        long pictureId = deleteRequest.getId();
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAM_ERROR, "图片id异常");
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(ObjectUtil.isNull(picture), ErrorCode.NOT_FOUND, "图片不存在");
        // 校验权限
        this.checkPictureAuth(loginUser, picture);
        // 事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "删除失败");
            // 更新空间额度
            if (picture.getSpaceId() != null) {
                boolean update = spaceService.lambdaUpdate().eq(Space::getId, picture.getSpaceId())
                        .setSql("totalSize=totalSize-" + picture.getPicSize())
                        .setSql("totalCount=totalCount-1").update();
                ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "删除文件失败，数据库异常");
            }
            // 异步清理文件
            this.deleteCosPicture(picture);
            return result;
        });
        return true;
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long pictureUserId = picture.getUserId();
        Long spaceId = picture.getSpaceId();
        Long loginUserId = loginUser.getId();
        // 公共图库，仅本人和管理员可操作
        if (spaceId == null) {
            if (!loginUserId.equals(pictureUserId) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
        } else { // 私有空间，仅本人可操作
            ThrowUtils.throwIf(!loginUserId.equals(pictureUserId), ErrorCode.NO_AUTH);
        }
    }

    @Override
    public boolean editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        ThrowUtils.throwIf(ObjectUtil.isNull(loginUser), ErrorCode.NOT_LOGIN);
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        this.validPicture(picture);
        // 校验是否存在
        Picture oldPicture = this.getById(picture.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldPicture), ErrorCode.NOT_FOUND, "数据不存在");
        // 权限校验
        this.checkPictureAuth(loginUser, oldPicture);
        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        // 修改
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return result;
    }
}




