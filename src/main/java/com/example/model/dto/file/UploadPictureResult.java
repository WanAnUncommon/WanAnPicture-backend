package com.example.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传文件结果
 *
 * @author WanAn
 */
@Data
public class UploadPictureResult implements Serializable {
    private static final long serialVersionUID = -6295298194400746441L;

    /**
     * 图片url
     */
    private String url;

    /**
     * 缩略图片url
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;


}
