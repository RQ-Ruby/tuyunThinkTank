package com.RQ.tuyunthinktank.controller;

import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.model.dto.UserLoginRequest;
import com.RQ.tuyunthinktank.model.dto.UserRegisterRequest;
import com.RQ.tuyunthinktank.model.vo.LoginUserVO;
import com.RQ.tuyunthinktank.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @description 用户接口
 * @className UserController
 * @author RQ
 * @date 2025/5/25 下午4:25
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserService userService;
/**
 * @description  用户注册
 * @return: com.RQ.tuyunthinktank.common.BaseResponse<java.lang.Long>
 * @author RQ
 * @date: 2025/5/31 下午4:09
 */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);

    }

   /**
    * @description  用户登录
    * @return: com.RQ.tuyunthinktank.common.BaseResponse<java.lang.Long>
    * @author RQ
    * @date: 2025/5/31 下午5:26
    */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }


}
