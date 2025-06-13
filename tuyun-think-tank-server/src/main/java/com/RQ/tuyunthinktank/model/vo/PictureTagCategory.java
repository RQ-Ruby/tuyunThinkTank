package com.RQ.tuyunthinktank.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PictureTagCategory {
    /*
    标签列表
     */
    private List<String> tagList;
    /*
    分类
     */
    private List<String> categoryList;
}
