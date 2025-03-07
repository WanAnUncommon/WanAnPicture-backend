package com.example.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.annotation.AuthCheck;
import com.example.common.BaseResponse;
import com.example.common.DeleteRequest;
import com.example.common.ResultUtils;
import com.example.constant.UserConstant;
import com.example.exception.ErrorCode;
import com.example.exception.ThrowUtils;
import com.example.manager.auth.SpaceUserAuthManager;
import com.example.manager.auth.StpKit;
import com.example.manager.auth.annotation.SaSpaceCheckPermission;
import com.example.manager.auth.model.SpaceUserPermissionConstant;
import com.example.model.dto.picture.*;
import com.example.model.entity.Picture;
import com.example.model.entity.Space;
import com.example.model.entity.User;
import com.example.model.enums.PictureReviewStatusEnum;
import com.example.model.vo.PictureTagCategoryVO;
import com.example.model.vo.PictureVO;
import com.example.service.PictureService;
import com.example.service.SpaceService;
import com.example.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 图片文件Controller
 *
 * @author WanAn
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 图片上传
     *
     * @param multipartFile        图片文件
     * @param pictureUploadRequest 图片上传信息
     * @param httpServletRequest   request
     * @return 图片上传结果
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 图片根据url上传
     *
     * @param pictureUploadRequest 图片上传信息
     * @param httpServletRequest   request
     * @return 图片上传结果
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest 图片删除信息
     * @param request       request
     * @return 删除结果
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(deleteRequest), ErrorCode.PARAM_ERROR, "参数为空");
        boolean result = pictureService.deletePicture(deleteRequest, userService.getLoginUser(request));
        return ResultUtils.success(result);
    }

    /**
     * 更新图片
     *
     * @param pictureUpdateRequest 图片更新信息
     * @param request              request
     * @return 更新结果
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureUpdateRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        pictureService.validPicture(picture);
        // 校验是否存在
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(oldPicture), ErrorCode.NOT_FOUND, "数据不存在");
        picture.setEditTime(new Date());
        // 填充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取图片
     *
     * @param id 图片id
     * @return 图片信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Picture> getPictureById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(picture), ErrorCode.NOT_FOUND, "数据不存在");
        return ResultUtils.success(picture);
    }

    /**
     * 根据id获取图片VO
     *
     * @param id 图片id
     * @return 图片信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVoById(long id,HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAM_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(picture), ErrorCode.NOT_FOUND, "数据不存在");
        Space space=null;
        if (picture.getSpaceId() != null) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH);
            space=spaceService.getById(picture.getSpaceId());
            ThrowUtils.throwIf(ObjectUtil.isEmpty(space), ErrorCode.NOT_FOUND, "空间不存在");
        }
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, userService.getLoginUser(request));
        PictureVO pictureVO = pictureService.getPictureVO(picture);
        pictureVO.setPermissionList(permissionList);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片
     *
     * @param pictureQueryRequest 图片查询信息
     * @return 图片信息
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureQueryRequest), ErrorCode.PARAM_ERROR, "参数为空");
        Page<Picture> page = pictureService.page(new Page<>(pictureQueryRequest.getCurrentPage(), pictureQueryRequest.getPageSize()),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(page);
    }

    /**
     * 分页获取图片VO
     *
     * @param pictureQueryRequest 图片查询信息
     * @return 图片信息
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVoByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureQueryRequest), ErrorCode.PARAM_ERROR, "参数为空");
        // 限制爬虫
        ThrowUtils.throwIf(pictureQueryRequest.getPageSize() > 50, ErrorCode.PARAM_ERROR, "参数过大");
        // 权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId == null) { // 只查公共图库
            pictureQueryRequest.setNullSpaceId(true);
            // 普通用户，只能查审核通过的
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        } else {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH);
        }
        Page<PictureVO> pictureVoPage = pictureService.listPictureVoByPage(pictureQueryRequest);
        return ResultUtils.success(pictureVoPage);
    }

    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureEditRequest), ErrorCode.PARAM_ERROR, "参数为空");
        boolean result = pictureService.editPicture(pictureEditRequest, userService.getLoginUser(request));
        return ResultUtils.success(result);
    }

    /**
     * 获取标签分类
     *
     * @return 标签分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategoryVO> getTagCategory() {
        List<String> categoryList = Arrays.asList("模版", "壁纸", "表情包", "美女", "风景", "二次元");
        List<String> tagList = Arrays.asList("高清", "青春", "电影", "cosplay", "汽车", "ppt");
        PictureTagCategoryVO pictureTagCategoryVO = new PictureTagCategoryVO();
        pictureTagCategoryVO.setCategoryList(categoryList);
        pictureTagCategoryVO.setTagList(tagList);
        return ResultUtils.success(pictureTagCategoryVO);
    }

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核信息
     * @return 成功
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<String> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureReviewRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success("操作成功");
    }

    /**
     * 图片批量上传
     *
     * @param pictureUploadByBatchRequest 图片批量上传信息
     * @return 成功上传的数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureUploadByBatchRequest), ErrorCode.PARAM_ERROR, "参数为空");
        User loginUser = userService.getLoginUser(request);
        Integer result = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 图片批量编辑
     *
     * @param pictureEditByBatchRequest 图片批量编辑信息
     * @return 成功
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isEmpty(pictureEditByBatchRequest), ErrorCode.PARAM_ERROR, "参数为空");
        boolean result = pictureService.editPictureByBatch(pictureEditByBatchRequest, userService.getLoginUser(request));
        return ResultUtils.success(result);
    }
}