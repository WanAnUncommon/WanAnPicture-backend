package com.example.api.imagesearch.model;

import lombok.Data;

/**
 * 以图搜图结果
 *
 * @author WanAn
 */
@Data
public class ImageSearchResult {
    // 缩略图链接
    private String thumbUrl;
    // 图片来源链接
    private String fromUrl;
}
