package com.RQ.tuyunthinktank.model.dto.space.analyze;

import lombok.Data;

import java.io.Serializable;
/**
 * @description 空间分析请求
 * @author RQ
 * @date 2025/9/29 上午10:11
 */
@Data
public class SpaceAnalyzeRequest implements Serializable {

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 是否查询公共图库
     */
    private boolean queryPublic;

    /**
     * 全空间分析
     */
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}
