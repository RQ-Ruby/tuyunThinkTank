package com.RQ.tuyunthinktank.model.dto.picture;

import lombok.Data;

@Data
public class PictureByBatchRequest {
  
    /**  
     * 搜索词  
     */  
    private String searchText;  
  
    /**  
     * 抓取数量  
     */  
    private Integer count = 10;

    /**
     * 图片名称
     */
    private String picName;
}
