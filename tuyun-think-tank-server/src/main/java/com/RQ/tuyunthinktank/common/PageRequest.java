package com.RQ.tuyunthinktank.common;

import lombok.Data;
/**
 * @description 通用的分页请求类
 * @author RQ
 * @date 2025/5/25 下午4:16
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
