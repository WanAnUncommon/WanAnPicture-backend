package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.DeleteRequest;
import com.example.model.dto.picture.*;
import com.example.model.entity.Picture;
import com.example.model.entity.User;
import com.example.model.vo.PictureVO;

/**
 * 针对表【picture(图片表)】的数据库操作Service
 *
 * @author WanAn
 */
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource          图片文件输入源
     * @param pictureUploadRequest 图片上传请求体
     * @param loginUser            登录用户
     * @return 图片返回信息
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest 图片查询请求体
     * @return 查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片返回信息
     *
     * @param picture 图片
     * @return 图片返回信息
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 获取图片分页信息
     *
     * @param picturePage 图片分页信息
     * @return 图片分页信息
     */
    Page<PictureVO> getPictureVoPage(Page<Picture> picturePage);

    /**
     * 校验图片
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求体
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 上传图片（批量）
     *
     * @param pictureUploadByBatchRequest 图片上传（批量）请求体
     * @param loginUser                   登录用户
     * @return 图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 获取图片分页信息（通过分页请求体）
     *
     * @param pictureQueryRequest 图片分页请求体
     * @return 图片分页信息
     */
    Page<PictureVO> listPictureVoByPage(PictureQueryRequest pictureQueryRequest);

    /**
     * 删除对象存储中的图片
     *
     * @param picture 图片
     */
    void deleteCosPicture(Picture picture);

    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求体
     * @param loginUser     登录用户
     * @return 是否删除成功
     */
    boolean deletePicture(DeleteRequest deleteRequest,User loginUser);

    /**
     * 校验图片权限
     *
     * @param loginUser 登录用户
     * @param picture   图片
     */
    void checkPictureAuth(User loginUser,Picture picture);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest 图片编辑请求体
     * @param loginUser          登录用户
     */
    boolean editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 编辑图片（批量）
     *
     * @param pictureEditByBatchRequest 图片编辑（批量）请求体
     * @param loginUser                 登录用户
     * @return 是否编辑成功
     */
    boolean editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);
}
