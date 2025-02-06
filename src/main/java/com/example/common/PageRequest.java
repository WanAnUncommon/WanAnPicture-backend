package com.example.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求类
 *
 * @author WanAn
 */
@Data
public class PageRequest implements Serializable {
    // 当前页码
    private int currentPage = 1;
    // 页大小
    private int pageSize = 10;
    // 排序字段
    private String sortField;
    // 排序顺序（默认升序）
    private String sortOrder = "descend";
}
