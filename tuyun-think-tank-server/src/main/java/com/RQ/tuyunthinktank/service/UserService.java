package com.RQ.tuyunthinktank.service;

import com.RQ.tuyunthinktank.model.dto.user.UserQueryRequest;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.LoginUserVO;
import com.RQ.tuyunthinktank.model.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
 * @description  将用户信息封装为 LoginUserVO(脱敏)
 * @return: com.RQ.tuyunthinktank.model.vo.LoginUserVO
 * @author RQ
 * @date: 2025/5/31 下午5:35
 */
    LoginUserVO getLoginUserVO(User user);

    /**
     * @description 将用户信息封装为 UserVO (脱敏)
     * @return: com.RQ.tuyunthinktank.model.vo.UserVO
     * @author RQ
     * @date: 2025/6/2 下午1:07
     */
    UserVO getUserVO(User user);

/**
 * @description 将用户信息封装为 UserVO<List>列表 (脱敏)
 * @return: java.util.List<com.RQ.tuyunthinktank.model.vo.UserVO>
 * @author RQ
 * @date: 2025/6/2 下午1:09
 */
    List<UserVO> getUserVOList(List<User> user);
/**
 * @description  获取当前登录用户
 * @param request
 * @return: com.RQ.tuyunthinktank.model.entity.User
 * @author RQ
 * @date: 2025/6/1 下午2:57
 */
    User getLoginUser(HttpServletRequest request);
/**
 * @description   分页查询
 * @return: com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.RQ.tuyunthinktank.model.entity.User>
 * @author RQ
 * @date: 2025/6/2 下午2:10
 */
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * @description  用户注销(退出登录)
     * @return: boolean
     * @author RQ
     * @date: 2025/6/1 下午3:32
     */
    boolean userLogout(HttpServletRequest request);

/**
 * @description  判断是否为管理员
 * @return: boolean
 * @author RQ
 * @date 2025/6/13 下午3:06
 */
    boolean isAdmin(User user);
}
