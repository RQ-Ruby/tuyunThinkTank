package com.RQ.tuyunthinktank.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.mapper.SpaceMapper;
import com.RQ.tuyunthinktank.model.dto.space.SpaceAddRequest;
import com.RQ.tuyunthinktank.model.dto.space.SpaceQueryRequest;
import com.RQ.tuyunthinktank.model.entity.Space;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.enums.SpaceLevelEnum;
import com.RQ.tuyunthinktank.model.vo.SpaceVO;
import com.RQ.tuyunthinktank.service.SpaceService;

import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author RQ
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-08-04 18:17:26
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;
    @Resource
    private TransactionTemplate transactionTemplate;  // 注入Spring的事务模板，用于编程式事务管理[6,7](@ref)
/**
 * @description 添加空间
 * @author RQ
 * @date 2025/8/15 下午7:58
 */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 将请求参数转换为实体对象
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);  // 使用工具类复制DTO属性到实体类

        // 设置默认值：若请求未提供空间名或级别，使用默认值
        if (StrUtil.isBlank(spaceAddRequest.getSpaceName())) {
            space.setSpaceName("默认空间");  // 默认空间名
        }
        if (spaceAddRequest.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());  // 默认空间级别为"普通"
        }

        this.fillSpaceBySpace(space);    // 根据空间级别填充额外数据（如权限配置等）
        this.validSpace(space, true);          // 校验空间数据合法性（如字段格式、唯一性等）

        Long userId = loginUser.getId();
        space.setUserId(userId);  // 设置空间所属用户ID

        // 权限校验：非管理员用户尝试创建非普通空间时抛出异常
        if (SpaceLevelEnum.COMMON.getValue() != spaceAddRequest.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }

        // 对用户ID加锁，防止并发下重复创建空间（锁对象需唯一）
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            // 使用事务模板执行数据库操作（确保原子性）
            Long newSpaceId = transactionTemplate.execute(status -> {
                // 检查用户是否已有空间（避免重复创建）
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");

                // 保存空间数据到数据库
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间创建失败");

                return space.getId();  // 返回新空间的ID
            });

            // 处理事务执行结果：若返回null则转为-1（表示失败）
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }
    /**
     * @description 校验空间
     * @author RQ
     * @date 2025/8/4 下午7:43
     */
    @Override
    public void validSpace(Space space, boolean add) {
        // 1. 基础空值校验
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间信息为空");

        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();

        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 2. 添加时，参数校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "空间等级错误");

        }
        // 3.修改数据时，如果要改空间级别
        if (spaceLevel != null && enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }

    }

    /**
     * @description 获取空间VO, 单条数据的封装类
     * @author RQ
     * @date 2025/8/4 下午7:55
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联用户信息（通过图片实体的 userId 字段）
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.OPERATION_ERROR, "关联用户不存在");
            spaceVO.setUser(userService.getUserVO(user));
        }

        return spaceVO;

    }

    /**
     * @description 分页对象转为VO
     * @author RQ
     * @date 2025/8/4 下午8:11
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * @description 获取查询条件
     * @author RQ
     * @date 2025/8/4 下午8:18
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        // 排序条件
        String sortField = spaceQueryRequest.getSortField();
        // 排序方向
        String sortOrder = spaceQueryRequest.getSortOrder();

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);


        // 添加排序条件（支持升序/降序）
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),
                sortOrder.equals("ascend"),  // 判断排序方向
                sortField);  // 排序字段

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
/**
 * @description 填充空间信息
 * @author RQ
 * @date 2025/8/4 下午8:32
 */
    @Override
    public void fillSpaceBySpace(Space space) {
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        //判断空间等级是否存在
        if(spaceLevelEnum!= null) {
            //设置空间大小和数量
            //默认空间大小和数量为空（管理员未未设置），则默认为等级对应的值
            if(space.getMaxSize() == null){
                space.setMaxSize(spaceLevelEnum.getMaxSize());
            }
            if (space.getMaxCount() == null) {
                space.setMaxCount(spaceLevelEnum.getMaxCount());
            }
        }

    }


}




