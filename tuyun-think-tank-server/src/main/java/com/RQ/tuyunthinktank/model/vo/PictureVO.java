package com.RQ.tuyunthinktank.model.vo;

import cn.hutool.json.JSONUtil;
import com.RQ.tuyunthinktank.model.entity.Picture;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
/**
 * @description 图片视图
 * @author RQ
 * @date 2025/6/11 上午10:59
 */

@Data
public class PictureVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片 url
     */
    private String url;
    /**
     * 缩略图 url
     */
    private String thumbnailUrl;


    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 标签
     */

    private List<String> tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 文件体积
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
     * 用户 id
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
     * 创建用户信息，保存用户信息，避免查询数据库
     */
    private UserVO user;
    //  序列化版本号，用于确保反序列化时版本的兼容性
    private static final long serialVersionUID = 1L;

    /**
     * @description 将vo对象转换为实体对象
     * @author RQ
     * @date 2025/6/9 下午5:50
     */
    public static Picture voToObj(PictureVO pictureVO) {
        // 如果传入的VO为空，直接返回null
        if (pictureVO == null) {
            return null;
        }
        // 创建目标实体对象
        Picture picture = new Picture();
        // 使用BeanUtils.copyProperties复制同名属性（需要两个类的属性名和类型匹配）
        BeanUtils.copyProperties(pictureVO, picture);
        // 特殊处理类型不同的字段：将List类型的tags转为JSON字符串
        picture.setTags(JSONUtil.toJsonStr(pictureVO.getTags()));
        return picture;
    }

  /**
     * @description 将实体对象转换为vo对象
     * @author RQ
     * @date 2025/6/9 下午5:50
   **/
    public static PictureVO objToVo(Picture picture) {
        // 如果传入的实体为空，直接返回null
        if (picture == null) {
            return null;
        }
        // 创建目标VO对象
        PictureVO pictureVO = new PictureVO();
        // 使用BeanUtils.copyProperties复制同名属性
        BeanUtils.copyProperties(picture, pictureVO);
        // 特殊处理类型不同的字段：将JSON字符串的tags转回List
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        return pictureVO;
    }
}
