package com.RQ.tuyunthinktank.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * @description  图片id，如果是修改图片，需要传入图片id
     * @param: id
     * @author RQ
     * @date: 2025/6/9 下午5:41
     */
    private Long id;

    //文件地址
    private  String url;
  
    private static final long serialVersionUID = 1L;  
}
