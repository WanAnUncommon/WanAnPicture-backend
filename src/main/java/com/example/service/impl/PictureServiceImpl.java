package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.exception.BusinessException;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.manager.FileManager;
import com.example.manager.upload.FilePictureUpload;
import com.example.manager.upload.PictureUploadTemplate;
import com.example.manager.upload.UrlPictureUpload;
import com.example.mapper.PictureMapper;
import com.example.model.dto.file.UploadPictureResult;
import com.example.model.dto.picture.PictureQueryRequest;
import com.example.model.dto.picture.PictureReviewRequest;
import com.example.model.dto.picture.PictureUploadRequest;
import com.example.model.entity.Picture;
import com.example.model.entity.User;
import com.example.model.enums.PictureReviewStatusEnum;
import com.example.model.vo.PictureVO;
import com.example.service.PictureService;
import com.example.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lenovo
 * @description 针对表【picture(图片表)】的数据库操作Service实现
 * @createDate 2025-02-11 15:13:12
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN);
        // 判断是否是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 是更新
        if (pictureId != null) {
            // 判断图片是否存在
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND, "图片不存在");
            // 仅自己或管理员可以更新
            if (!loginUser.getId().equals(oldPicture.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
        }
        // 上传图片
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 根据文件输入源选择上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 存入数据库
        Picture picture = new Picture();
        BeanUtil.copyProperties(uploadPictureResult, picture);
        picture.setUserId(loginUser.getId());
        // 填充审核参数
        this.fillReviewParams(picture, loginUser);
        // 如果是更新
        if (pictureId != null) {
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean save = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "上传文件失败，数据库异常");
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
        queryWrapper.like(CharSequenceUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        if (searchTest != null) {
            queryWrapper.and(qw -> {
                qw.like("name", searchTest);
                qw.or();
                qw.like("introduction", searchTest);
            });
        }
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
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
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
}




