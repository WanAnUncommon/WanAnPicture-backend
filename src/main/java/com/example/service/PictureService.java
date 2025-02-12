package com.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.model.dto.picture.PictureQueryRequest;
import com.example.model.dto.picture.PictureUploadRequest;
import com.example.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.model.entity.User;
import com.example.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author lenovo
* @description 针对表【picture(图片表)】的数据库操作Service
* @createDate 2025-02-11 15:13:12
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param multipartFile 图片文件
     * @param pictureUploadRequest 图片上传请求体
     * @param loginUser 登录用户
     * @return 图片返回信息
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

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
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 校验图片
     *
     * @param picture 图片
     */
    void validPicture(Picture picture);
}
