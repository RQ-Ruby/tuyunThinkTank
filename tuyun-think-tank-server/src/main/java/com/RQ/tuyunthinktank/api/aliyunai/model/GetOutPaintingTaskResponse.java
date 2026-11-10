package com.RQ.tuyunthinktank.api.aliyunai.model;

import lombok.Data;

import java.util.List;

@Data
public class GetOutPaintingTaskResponse {

    /**
     * 请求唯一标识。
     */
    private String requestId;

    /**
     * 输出信息。
     */
    private Output output;

    public GetOutPaintingTaskResponse(String requestId, Output output) {
        this.requestId = requestId;
        this.output = output;
    }

    public GetOutPaintingTaskResponse() {
    }

    @Data
    public static class Output {
        private String taskId;
        private String taskStatus;
        private String submitTime;
        private String scheduledTime;
        private String endTime;

        /**
         * 兼容旧接口的单张结果字段；新接口会从 results 中回填。
         */
        private String outputImageUrl;

        /**
         * 万相通用图像编辑接口返回的结果列表。
         */
        private List<Result> results;

        private String code;
        private String message;
        private TaskMetrics taskMetrics;
    }

    @Data
    public static class Result {
        private String url;
        private String code;
        private String message;
    }

    @Data
    public static class TaskMetrics {
        private Integer total;
        private Integer succeeded;
        private Integer failed;
    }
}