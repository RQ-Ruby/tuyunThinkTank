package com.RQ.tuyunthinktank.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * @description 图片修改请求体，普通用户修改图片信息使用，范围更小
 * @author RQ
 * @date 2025/6/13 下午2:59
 */
@Data
public class PictureEditRequest implements Serializable {
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    private String name;  
  
    /**  
     * 简介  
     */  
    private String introduction;  
  
    /**  
     * 分类  
     */  
    private String category;  
  
    /**  
     * 标签  
     */  
    private List<String> tags;
  
    private static final long serialVersionUID = 1L;  
}
