package com.example.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求
 *
 * @author WanAn
 **/
@Data
public class PictureUploadDTO implements Serializable {
    private static final long serialVersionUID = 6517468443979095768L;
    private Long id;
}
