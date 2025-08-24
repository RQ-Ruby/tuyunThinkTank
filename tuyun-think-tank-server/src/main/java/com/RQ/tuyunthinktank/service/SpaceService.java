package com.RQ.tuyunthinktank.service;

import com.RQ.tuyunthinktank.model.dto.space.SpaceAddRequest;
import com.RQ.tuyunthinktank.model.dto.space.SpaceQueryRequest;
import com.RQ.tuyunthinktank.model.entity.Space;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author RQ
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-08-04 18:17:26
 */
public interface SpaceService extends IService<Space> {
    /**
     * @description 添加空间
     * @author RQ
     * @date 2025/8/15 下午7:59
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验
     *
     * @param space
     * @param add
     */
    public void validSpace(Space space, boolean add);

    /**
     * @description 获取空间VO
     * @author RQ
     * @date 2025/8/4 下午7:55
     */
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * @description 分页对象转为VO
     * @author RQ
     * @date 2025/8/4 下午8:09
     */
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * @description 获取查询条件
     * @author RQ
     * @date 2025/8/4 下午8:10
     */
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * @description 填充空间信息
     * @author RQ
     * @date 2025/8/4 下午8:11
     */
    public void fillSpaceBySpace(Space  space);
}
