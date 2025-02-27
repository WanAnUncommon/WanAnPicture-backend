package com.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 空间表
 * @TableName space
 */
@TableName(value ="space")
@Data
public class Space implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间等级：0-普通空间，1-高级空间，2-超级空间
     */
    private Integer spaceLevel;

    /**
     * 最大空间大小
     */
    private Integer maxSize;

    /**
     * 最大文件数量
     */
    private Integer maxCount;

    /**
     * 当前文件总大小
     */
    private Integer totalSize;

    /**
     * 当前文件总数量
     */
    private Integer totalCount;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0否 1是
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}