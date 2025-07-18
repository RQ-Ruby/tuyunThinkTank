package com.RQ.tuyunthinktank.controller;

import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author RQ
 * @description 测试
 * @date 2025/5/25 下午4:25
 */
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * @return: com.RQ.tuyunthinktank.common.BaseResponse<java.lang.String>
     * @author RQ
     * @date: 2025/5/25 下午4:26
     */
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("基础框架搭建完毕");
    }
}
