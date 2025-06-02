package com.RQ.tuyunthinktank.controller;

import com.RQ.tuyunthinktank.annotation.AuthCheck;
import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.DeleteRequest;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.constant.UserConstant;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.model.dto.user.*;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.LoginUserVO;
import com.RQ.tuyunthinktank.model.vo.UserVO;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

   /**
    * @description  获取当前登录用户
    * @author RQ
    * @date 2025/6/1 下午3:13
    */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

   /**
    * @description  用户注销
    * @author RQ
    * @date 2025/6/1 下午3:33
    */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }
    /**
     * 创建用户
     */
    @PostMapping("/add")
    // 仅管理员可调用
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        //如果请求为空，抛出异常
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        //将请求中的参数复制到user对象中
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        // 密码加密
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        // 将加密后的密码设置到user对象中
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        // 如果保存失败，抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        // 如果id小于等于0，抛出异常
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    //DeleteRequest 是一个自定义的请求类，用于封装删除请求的参数。
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    // 仅管理员可调用
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        // 如果更新失败，抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // current 当前页，pageSize 每页显示的数量
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        //userService.page() 方法用于分页查询用户数据,getQueryWrapper() 方法用于构建查询条件,
        // userQueryRequest 是查询请求参数
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        //new Page<>(current, pageSize) 用于创建一个分页对象，current 是当前页码，pageSize 是每页显示的数量。
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        //获得用户记录列表，再将列表转换为 UserVO 对象列表。
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        //将转换后的 UserVO 对象列表设置到 userVOPage 对象中。
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }


}
