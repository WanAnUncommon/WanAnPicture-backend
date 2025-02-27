package com.example.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求
 *
 * @author WanAn
 **/
@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 6517468443979095768L;

    // 图片id
    private Long id;

    // 图片地址
    private String fileUrl;

    // 图片名称
    private String picName;

    // 空间id
    private Long spaceId;
}
