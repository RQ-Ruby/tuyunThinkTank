package com.RQ.tuyunthinktank.service;

import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.LoginUserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author RQ
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-31 10:58:26
*/
public interface UserService extends IService<User> {

    /**
     * @description  用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return: long
     * @author RQ
     * @date: 2025/5/31 下午3:41
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

/**
 * @description  密码加密
 * @return: java.lang.String
 * @author RQ
 * @date: 2025/5/31 下午5:36
 */
    String getEncryptPassword(String userPassword);

   /**
    * @description 用户登录
    * @author RQ
    * @date 2025/5/31 下午5:18
    */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);
/**
 * @description  将用户信息封装为 LoginUserVO
 * @return: com.RQ.tuyunthinktank.model.vo.LoginUserVO
 * @author RQ
 * @date: 2025/5/31 下午5:35
 */
    LoginUserVO getLoginUserVO(User user);
}
