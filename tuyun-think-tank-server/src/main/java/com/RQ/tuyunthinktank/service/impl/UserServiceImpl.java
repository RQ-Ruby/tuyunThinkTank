package com.RQ.tuyunthinktank.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.RQ.tuyunthinktank.constant.UserConstant;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.model.vo.LoginUserVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.service.UserService;
import com.RQ.tuyunthinktank.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

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

}




