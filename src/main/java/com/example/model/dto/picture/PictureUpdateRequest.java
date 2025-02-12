package com.example.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片更新请求
 *
 * @author WanAn
 **/
@Data
public class PictureUpdateRequest implements Serializable {
    private static final long serialVersionUID = 4941781157928876216L;
    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签(Json数组)
     */
    private List<String> tags;


}
