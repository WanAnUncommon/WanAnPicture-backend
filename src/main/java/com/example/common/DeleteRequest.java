package com.example.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求类
 *
 * @author WanAn
 */
@Data
public class DeleteRequest implements Serializable {
    // 当id
    private long id;
}
