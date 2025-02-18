package com.example.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 批量抓取图片上传请求
 *
 * @author WanAn
 **/
@Data
public class PictureUploadByBatchRequest implements Serializable {
    private static final long serialVersionUID = 1882362751776508719L;

    // 抓取数量
    private Integer count;

    // 搜索词
    private String searchText;

    // 名称前缀
    private String namePrefix;
}
