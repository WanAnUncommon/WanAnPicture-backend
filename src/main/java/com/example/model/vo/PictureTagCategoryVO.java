package com.example.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 图片标签分类VO
 *
 * @author WanAn
 */
@Data
public class PictureTagCategoryVO {
    // 标签列表
    private List<String> tagList;
    // 分类列表
    private List<String> categoryList;
}
