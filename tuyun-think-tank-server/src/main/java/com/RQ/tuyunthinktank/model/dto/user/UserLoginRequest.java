package com.RQ.tuyunthinktank.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @description 用户登录请求体
 * @author RQ
 * @date 2025/5/31 上午11:42
 */

@Data
public class UserLoginRequest implements Serializable {
//快捷键alt+Interface 实现Serializable接口，实现序列化和反序列化
    private static final long serialVersionUID = 1514641658370860145L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;


}

