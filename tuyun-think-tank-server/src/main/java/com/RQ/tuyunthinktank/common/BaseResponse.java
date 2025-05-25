package com.RQ.tuyunthinktank.common;

import com.RQ.tuyunthinktank.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @description 全局响应封装类
 * @author RQ
 * @date 2025/5/25 下午4:17
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码，表示请求结果的状态，如成功、参数错误等
     */
    private int code;

    /**
     * 响应数据，泛型类型，根据实际返回内容而定
     */
    private T data;

    /**
     * 描述信息，用于更直观地展示请求结果的含义
     */
    private String message;

    /**
     * 全参构造方法，用于创建包含状态码、数据和描述信息的响应对象
     *
     * @param code    状态码
     * @param data    响应数据
     * @param message 描述信息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 无描述信息的构造方法，默认描述信息为空字符串
     *
     * @param code 状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 根据 ErrorCode 枚举创建响应对象，使用枚举中的状态码和描述信息
     *
     * @param errorCode 错误码枚举对象
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

