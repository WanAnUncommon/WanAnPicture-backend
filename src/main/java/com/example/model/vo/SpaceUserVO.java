package com.example.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.example.model.entity.SpaceUser;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 空间用户视图
 *
 * @author WanAn
 */
@Data
public class SpaceUserVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 空间角色：viewer-浏览者，editor-编辑者，admin-管理员
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户
     */
    private UserVO userVO;

    /**
     * 空间
     */
    private SpaceVO spaceVO;

    private static final long serialVersionUID = 110076839538483082L;

    /**
     * 对象转包装类
     *
     * @param spaceUser 实体类
     * @return 包装类
     */
    public static SpaceUserVO objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtil.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }

    /**
     * 包装类转对象
     *
     * @param spaceUserVO 包装类
     * @return 对象
     */
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserVO, spaceUser);
        return spaceUser;
    }
}