package com.example.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改空间请求
 *
 * @author WanAn
 */
@Data
public class SpaceEditRequest implements Serializable {
    private static final long serialVersionUID = 6319008113804983030L;
    private String spaceName;
    // 空间id
    private Long id;
}
