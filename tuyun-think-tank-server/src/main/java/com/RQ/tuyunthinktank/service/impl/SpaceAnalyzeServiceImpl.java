package com.RQ.tuyunthinktank.service.impl;

import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.mapper.SpaceMapper;
import com.RQ.tuyunthinktank.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.model.entity.Space;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.service.SpaceAnalyzeService;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.annotation.Resource;

/**
 * @description 空间分析服务实现类
 * @author RQ
 * @date 2025/9/29 上午10:13
 */
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceAnalyzeService {
    @Resource
    private UserService userService;

    /**
     * @description 检查空间分析权限
     * @author RQ
     * @date 2025/9/29 上午10:13
     */
    public void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        //公共图库分析||全空间分析-仅限管理员
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            //检查是否为管理员
            if (!userService.isAdmin(loginUser)) {
                throw new RuntimeException("非管理员用户无权限进行全空间分析和公共图库分析");
            }
        }
        //私密空间-仅空间创建者或者管理员
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        if (spaceId == null) {
            throw new RuntimeException("无效空间ID");
        }
       //检查用户是否是空间创建者或者管理员
        if (!userService.isSpaceCreatorOrAdmin(spaceId, loginUser)) {
            throw new RuntimeException("非空间创建者或管理员用户无权限进行私密空间分析");
        }

    }

    /**
     * @description 封装查询条件
     * @author RQ
     * @date 2025/9/29 上午10:13
     */
   public void fillQueryWrapper(QueryWrapper<Picture> queryWrapper, SpaceAnalyzeRequest spaceAnalyzeRequest) {
        //根据请求参数填充查询条件
       //全空间分析-查询所有空间
        if (spaceAnalyzeRequest.isQueryAll()) {
            return ;
        }
       //公共图库分析-查询所有公共空间
        if (spaceAnalyzeRequest.isQueryPublic()) {
      queryWrapper.isNull("spaceId");
      return ;
        }
        //私密空间-查询指定空间图片
        if (spaceAnalyzeRequest.getSpaceId() != null) {
            queryWrapper.eq("spaceId", spaceAnalyzeRequest.getSpaceId());
            return ;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间分析请求参数错误");

    }




}