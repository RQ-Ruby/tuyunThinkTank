package com.RQ.tuyunthinktank.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建万相图像编辑（扩图）任务请求参数。
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {

    /**
     * 万相通用图像编辑模型。
     */
    private String model = "wanx2.1-imageedit";

    /**
     * 输入图像与编辑指令。
     */
    private Input input;

    /**
     * 图像处理参数。
     */
    private Parameters parameters;

    @Data
    public static class Input {
        /**
         * 扩图提示词，用来说明希望扩展出的画面内容。
         */
        private String prompt;

        /**
         * 图像编辑功能，扩图固定使用 expand。
         */
        private String function;

        /**
         * 需要扩图的公开图片地址。
         */
        @Alias("base_image_url")
        @JsonProperty("base_image_url")
        private String baseImageUrl;
    }

    @Data
    public static class Parameters implements Serializable {
        /**
         * 生成图片数量，范围 1~4。
         */
        private Integer n = 1;

        /**
         * 随机种子，可选。
         */
        private Long seed;

        /**
         * 是否添加 AI 生成水印。
         */
        private Boolean watermark = false;
    }
}