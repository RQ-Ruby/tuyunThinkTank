package com.RQ.tuyunthinktank.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * @description 空间等级
 * @author RQ
 * @date 2025/8/24 下午4:19
 */
@Data
@AllArgsConstructor
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
