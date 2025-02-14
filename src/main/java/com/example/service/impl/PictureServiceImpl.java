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
import com.example.mapper.PictureMapper;
import com.example.model.dto.file.UploadPictureResult;
import com.example.model.dto.picture.PictureQueryRequest;
import com.example.model.dto.picture.PictureUploadRequest;
import com.example.model.entity.Picture;
import com.example.model.entity.User;
import com.example.model.vo.PictureVO;
import com.example.service.PictureService;
import com.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN);
        // 判断是否是更新
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 是更新
        if (pictureId != null) {
            // 判断图片是否存在
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND, "图片不存在");
        }
        // 上传图片
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 存入数据库
        Picture picture = new Picture();
        BeanUtil.copyProperties(uploadPictureResult, picture);
        picture.setUserId(loginUser.getId());
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
}




