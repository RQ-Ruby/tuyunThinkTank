package com.RQ.tuyunthinktank.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.RQ.tuyunthinktank.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.RQ.tuyunthinktank.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.RQ.tuyunthinktank.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author RQ
 * @description 阿里云服务交互
 * @date 2025/9/22 下午4:23
 */
@Slf4j  // 使用Lombok注解，自动生成日志记录器
@Component  // 声明为Spring组件，由Spring容器管理
public class AliYunAiApi {

    // 从配置文件中读取阿里云AI服务的API密钥
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 定义创建图像扩展任务的API端点URL
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 定义查询任务状态的API端点URL，%s为任务ID占位符
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建图像扩展任务
     *
     * @param createOutPaintingTaskRequest 包含扩图参数的请求对象
     * @return 创建任务的响应结果
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        // 检查请求参数是否为空
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        // 构建HTTP POST请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)  // 添加认证头
                // 异步模式（X-DashScope-Async）必须设置为 enable以启用异步处理
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())  // 设置内容类型为JSON
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));  // 将请求对象转换为JSON字符串并设置为请求体
        // 使用try-with-resources确保HttpResponse被正确关闭
        try (HttpResponse httpResponse = httpRequest.execute()) {
            // 检查HTTP响应状态是否成功
            if (!httpResponse.isOk()) {
                log.error("请求异常：{}", httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            //  使用Hutool的JSONUtil将响应体解析为Java对象
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            String errorCode = response.getCode();
            // 检查响应中是否包含错误码
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI 扩图失败，errorCode:{}, errorMessage:{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
            }
            return response;
        }
    }

    /**
     * 查询已创建的任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态的响应结果
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        // 检查任务ID是否为空
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        // 构建HTTP GET请求查询任务状态，将任务ID填充到URL中
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)  // 添加认证头
                .execute()) {
            // 检查HTTP响应状态是否成功
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
            // 将响应体解析为GetOutPaintingTaskResponse对象
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}