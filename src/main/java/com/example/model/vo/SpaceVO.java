package com.example.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.example.model.entity.Space;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间视图
 *
 * @author WanAn
 */
@Data
public class SpaceVO implements Serializable {
    /**
     * id
     */
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
     * 空间类型：0-私有空间，1-团队空间
     */
    private Integer spaceType;

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
     * 创建用户
     */
    private UserVO userVO;

    private static final long serialVersionUID = 6639265439739795479L;

    /**
     * 对象转包装类
     *
     * @param space 实体类
     * @return 包装类
     */
    public static SpaceVO objToVo(Space space) {
        if (space == null) {
            return null;
        }
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }

    /**
     * 包装类转对象
     *
     * @param spaceVO 包装类
     * @return 对象
     */
    public static Space voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);
        return space;
    }
}