package com.RQ.tuyunthinktank.model.dto.space;

import lombok.Data;

/**
 * @author RQ
 * @description 空间等级
 * @date 2025/8/24 下午4:19
 */
@Data
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;

    public SpaceLevel(int value, String text, long maxCount, long maxSize) {
        this.value = value;
        this.text = text;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }
}
