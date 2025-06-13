package com.RQ.tuyunthinktank.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.RQ.tuyunthinktank.constant.UserConstant;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.model.dto.user.UserQueryRequest;
import com.RQ.tuyunthinktank.model.enums.UserRoleEnum;
import com.RQ.tuyunthinktank.model.vo.LoginUserVO;
import com.RQ.tuyunthinktank.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.service.UserService;
import com.RQ.tuyunthinktank.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author RQ
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-31 10:58:26
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
/**
 * @description 用户注册
 * @param userAccount 用户账号
 * @param userPassword 用户密码
 * @param checkPassword 校验密码
 * @return: long
 * @author RQ
 * @date: 2025/5/31 下午3:41
 */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验
        if (ObjUtil.isEmpty(userAccount) || ObjUtil.isEmpty(userPassword) || ObjUtil.isEmpty(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        //2.密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //3.账户不能重复
        //query() 方法用于创建一个查询对象，用于构建查询条件。
        //eq 方法用于添加等于条件，即查询 userAccount 等于指定值的记录。
        //count() 方法用于执行查询并返回匹配条件的记录数量。
        //在这个例子中，query().eq("userAccount", userAccount).count() 表示查询 userAccount 等于 userAccount 的记录数量。
        long count = query().eq("userAccount", userAccount).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //4.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //5.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        //this 是当前类的实例，通常用于调用当前类的方法或访问当前类的属性。
        //在这个例子中，this 用于调用当前类的 save 方法，将 user 对象保存到数据库中。
        //this.save(user) 表示将 user 对象保存到数据库中。
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }
/**
 * @description 密码加密
 * @param userPassword 用户密码
 * @return: java.lang.String
 * @author RQ
 * @date: 2025/5/31 下午3:45
 */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "RuQian";
        // 加密方式
        //DigestUtils.md5DigestAsHex 方法用于计算给定字符串的 MD5 哈希值，并将其作为十六进制字符串返回。
        //在这个例子中，(SALT + userPassword) 表示将盐值和用户密码拼接在一起，然后计算它们的 MD5 哈希值。
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
    /**
     * @description  用户登录
     * @return: com.RQ.tuyunthinktank.model.vo.LoginUserVO
     * @author RQ
     * @date: 2025/5/31 下午5:20
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
       //1.校验
        if (ObjUtil.isEmpty(userAccount) || ObjUtil.isEmpty(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        //2.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户是否存在
        User user = query()                  // 1. 创建查询对象（QueryWrapper）
                .eq("userAccount", userAccount)   // 2. 添加账号等值条件
                .eq("userPassword", encryptPassword) // 3. 添加密码等值条件
                .one();                           // 4. 执行查询并返回单个结果
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //3.用户脱敏
        LoginUserVO loginUserVO = getLoginUserVO(user);
        //4.记录用户的登录态
        //request.getSession().setAttribute 方法用于将指定的对象存储到当前会话的属性中。
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUserVO);
        return loginUserVO;
    }

   /**
    * @description  用户脱敏
    * @return: com.RQ.tuyunthinktank.model.vo.LoginUserVO
    * @author RQ
    * @date: 2025/5/31 下午5:23
    */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * @description  将用户信息封装为 UserVO (脱敏)
     * @return: com.RQ.tuyunthinktank.model.vo.UserVO
     * @author RQ
     * @date: 2025/6/2 下午1:11
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
/**
 * @description  将用户信息封装为 UserVO<List>列表 (脱敏)
 * @return: java.util.List<com.RQ.tuyunthinktank.model.vo.UserVO>
 * @author RQ
 * @date: 2025/6/2 下午1:11
 */
    @Override
    public List<UserVO> getUserVOList(List<User> user) {
        if (user == null) {
            return null;
        }
        // 使用 Stream API 进行函数式编程转换
        return user.stream()          // 1. 将 List<User> 转换为 Stream<User> 流
                .map(this::getUserVO)     // 2. 映射操作：对每个 User 对象调用 getUserVO 方法转换为 UserVO 对象
                .collect(                 // 3. 终止操作：将流元素收集到新集合
                        Collectors.toList()  // 4. 收集器：将流元素收集到 List<UserVO> 中
                );
    }

    /**
     * @description  获取当前登录用户
     * @param request
     * @return: com.RQ.tuyunthinktank.model.entity.User
     * @author RQ
     * @date: 2025/6/1 下午2:58
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        //1.先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        // 修改这里：将强制转换为LoginUserVO
        LoginUserVO currentUser = (LoginUserVO) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2.从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        // 保持返回User类型
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }
/**
 * @description   分页查询
 * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.RQ.tuyunthinktank.model.entity.User>
 * @author RQ
 * @date: 2025/6/2 下午2:11
 */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

/**
 * @description  用户注销
 * @author RQ
 * @date 2025/6/13 下午3:41
 */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        //1.先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj  == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //2.移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }
/**
 * @description  是否为管理员
 * @author RQ
 * @date 2025/6/13 下午3:41
 */
@Override
public boolean isAdmin(User user) {
    // 使用短路与运算保证空指针安全
    return user != null
            && UserRoleEnum.ADMIN.getValue()  // 获取管理员角色标识
            .equals(user.getUserRole());  // 比对用户角色字段
}


}




