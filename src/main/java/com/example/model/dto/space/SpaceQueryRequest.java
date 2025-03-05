package com.example.model.dto.space;

import com.example.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询空间请求
 *
 * @author WanAn
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -8297225395030728086L;
    private String spaceName;
    // 空间id
    private Long id;
    // 用户id
    private Long userId;
    // 空间等级
    private Integer spaceLevel;

    /**
     * 空间类型：0-私有空间，1-团队空间
     */
    private Integer spaceType;
}
