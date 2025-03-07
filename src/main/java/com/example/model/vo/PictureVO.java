package com.example.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.example.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 图片视图
 *
 * @author WanAn
 */
@Data
public class PictureVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 图片url
     */
    private String url;

    /**
     * 缩略图片url
     */
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片简介
     */
    private String introduction;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tags;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

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

    // 权限列表
    private List<String> permissionList=new ArrayList<>();

    private static final long serialVersionUID = 3908504123233497337L;

    /**
     * 对象转包装类
     *
     * @param picture 实体类
     * @return 包装类
     */
    public static PictureVO objToVo(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtil.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }

    /**
     * 包装类转对象
     *
     * @param pictureVO 包装类
     * @return 对象
     */
    public static Picture voToObj(PictureVO pictureVO) {
        if (pictureVO == null) {
            return null;
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureVO, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }
}