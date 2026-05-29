package com.RQ.tuyunthinktank.controller;
import cn.hutool.json.JSONUtil;
import com.RQ.tuyunthinktank.annotation.AuthCheck;
import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.DeleteRequest;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.constant.UserConstant;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.manage.auth.SpaceUserAuthManager;
import com.RQ.tuyunthinktank.model.dto.space.*;
import com.RQ.tuyunthinktank.model.entity.Space;
import com.RQ.tuyunthinktank.model.entity.SpaceUser;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.enums.SpaceLevelEnum;
import com.RQ.tuyunthinktank.model.enums.UserRoleEnum;
import com.RQ.tuyunthinktank.model.vo.SpaceVO;
import com.RQ.tuyunthinktank.service.SpaceService;
import com.RQ.tuyunthinktank.service.SpaceUserService;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author RQ
 * @description 空间管理
 * @date 2025/6/12 下午2:53
 */
@RestController
@Slf4j
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;
    @Resource
    private SpaceUserService spaceUserService;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;





/**
 * @description 获取空间级别列表
 * @author RQ
 * @date 2025/8/24 下午4:22
 */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        // 将枚举值流式处理为SpaceLevel对象列表
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum ->
                        // 构造空间级别传输对象
                        new SpaceLevel(
                                spaceLevelEnum.getValue(),     // 级别标识值
                                spaceLevelEnum.getText(),      // 级别描述文本
                                spaceLevelEnum.getMaxCount(),  // 允许的最大文件数
                                spaceLevelEnum.getMaxSize()    // 允许的最大存储空间(单位：字节)
                        )
                )
                .collect(Collectors.toList());

        // 包装为成功响应
        return ResultUtils.success(spaceLevelList);
    }

    /**
     * @description 添加空间
     * @author RQ
     * @date 2025/6/13 下午3:04
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 校验请求参数是否为空
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务层添加空间
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }

    /**
     * @description 空间删除
     * @author RQ
     * @date 2025/6/13 下午4:38
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 1. 参数校验（使用ThrowUtils统一校验逻辑）
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 2. 获取登录用户（带登录态校验）
        User loginUser = userService.getLoginUser(request);
        // 3. 校验空间存在性（使用NOT_FOUND_ERROR更准确）
        Long id = deleteRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 4. 权限校验（管理员或资源所有者）
        if (!loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())
                && !oldSpace.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 5. 执行删除并校验结果
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间删除失败");
        // 6. 级联删除空间成员记录（团队空间）
        spaceUserService.lambdaUpdate()
                .eq(SpaceUser::getSpaceId, id)
                .remove();

        log.info("空间删除成功 ID:{} 操作者:{}", id, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * @description 更新空间（仅管理员可用）
     * @author RQ
     * @date 2025/6/13 下午5:00
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
                                               HttpServletRequest request) {
        // 1. 基础参数校验
        ThrowUtils.throwIf(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 2. 获取登录用户（带权限校验）
        User loginUser = userService.getLoginUser(request);
        // 3. 转换DTO到实体
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 填充创建信息
        spaceService.fillSpaceBySpace(space);
        // 4. 业务校验（包含权限校验）
        Space oldSpace = spaceService.getById(space.getId());

        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        spaceService.validSpace(space,false);

        // 6. 执行更新操作
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间更新失败");

        log.info("空间更新成功 ID:{} 操作者:{}", space.getId(), loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * @description 根据 id 获取空间（仅管理员可用）
     * @author RQ
     * @date 2025/6/13 下午5:08
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 获取封装类
        return ResultUtils.success(space);
    }

    /**
     * @description 根据 id 获取空间（封装类）
     * @author RQ
     * @date 2025/6/13 下午5:09
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 查询数据库
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space== null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVO.setPermissionList(permissionList);
        // 获取封装类
        return ResultUtils.success(spaceVO);
    }

    /**
     * @description 分页获取空间列表（仅管理员可用）
     * @author RQ
     * @date 2025/6/13 下午5:18
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //执行分页查询
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                //spaceService.getQueryWrapper()构建MyBatis-Plus查询条件
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * @description 分页获取空间列表（封装类）
     * @author RQ
     * @date 2025/6/13 下午6:16
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                         HttpServletRequest request) { // 移除 request 参数
        // 参数校验（增强健壮性）
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || size <= 0, ErrorCode.PARAMS_ERROR, "分页参数错误");
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "单页数量不能超过20");

        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取脱敏数据（包含关联用户信息）
        Page<SpaceVO> voPage = spaceService.getSpaceVOPage(spacePage,request);

        // 修改日志记录方式（移除登录用户依赖）
        log.info("空间分页查询成功 当前页:{}", current);
        return ResultUtils.success(voPage);
    }

    /**
     * @description 编辑空间(创建者或管理员)
     * @author RQ
     * @date 2025/6/13 下午6:58
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest,
                                             HttpServletRequest request) {
        // 1. 基础参数校验
        ThrowUtils.throwIf(spaceEditRequest == null || spaceEditRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 2. 转换DTO到实体
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 填充创建信息
        spaceService.fillSpaceBySpace(space);
        // 更新编辑时间
        space.setEditTime(new Date());
        // 3. 业务规则校验
        User loginUser = userService.getLoginUser(request);
        Space oldSpace = spaceService.getById(space.getId());
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 权限校验：仅允许编辑自己的空间或管理员
        ThrowUtils.throwIf(!oldSpace.getUserId().equals(loginUser.getId()) &&
                        !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR);
        spaceService.validSpace(space,false);
        // 5. 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "空间更新失败");

        log.info("空间编辑成功 ID:{} 操作者:{}", space.getId(), loginUser.getId());
        return ResultUtils.success(true);
    }








}
