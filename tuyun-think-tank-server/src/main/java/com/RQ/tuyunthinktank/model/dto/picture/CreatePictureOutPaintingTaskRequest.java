package com.RQ.tuyunthinktank.model.dto.picture;

import com.RQ.tuyunthinktank.api.aliyunai.model.CreateOutPaintingTaskRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author RQ
 * @description 创建图片扩图任务请求参数
 * @date 2025/9/22 下午4:44
 */
@Data
public class CreatePictureOutPaintingTaskRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 扩图参数
     */
    private CreateOutPaintingTaskRequest.Parameters parameters;

    private static final long serialVersionUID = 1L;
}
