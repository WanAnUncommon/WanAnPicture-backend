package com.example.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 图片批量编辑请求
 *
 * @author WanAn
 **/
@Data
public class PictureEditByBatchRequest implements Serializable {
    private static final long serialVersionUID = 4516248769087027273L;
    /**
     * id列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签(Json数组)
     */
    private List<String> tags;

    /**
     * 图片名称规则
     */
    private String nameRule;
}
