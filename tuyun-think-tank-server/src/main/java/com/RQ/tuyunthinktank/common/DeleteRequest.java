package com.RQ.tuyunthinktank.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 删除目标的唯一标识
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

