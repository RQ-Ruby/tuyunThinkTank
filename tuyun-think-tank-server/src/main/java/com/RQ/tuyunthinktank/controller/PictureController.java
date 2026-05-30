package com.RQ.tuyunthinktank.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.RQ.tuyunthinktank.annotation.AuthCheck;
import com.RQ.tuyunthinktank.annotation.RateLimiter;
import com.RQ.tuyunthinktank.api.aliyunai.AliYunAiApi;
import com.RQ.tuyunthinktank.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.RQ.tuyunthinktank.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.DeleteRequest;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.constant.UserConstant;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.manage.auth.SpaceUserAuthManager;
import com.RQ.tuyunthinktank.manage.auth.StpKit;
import com.RQ.tuyunthinktank.manage.auth.annotation.SaSpaceCheckPermission;
import com.RQ.tuyunthinktank.manage.auth.model.SpaceUserPermissionConstant;
import com.RQ.tuyunthinktank.model.dto.picture.*;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.model.entity.Space;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.enums.PictureReviewStatusEnum;
import com.RQ.tuyunthinktank.model.enums.UserRoleEnum;
import com.RQ.tuyunthinktank.model.vo.PictureTagCategory;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.RQ.tuyunthinktank.service.PictureService;
import com.RQ.tuyunthinktank.service.SpaceService;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author RQ
 * @description 图片管理
 * @date 2025/6/12 下午2:53
 */
@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SpaceService spaceService;
    @Resource
    private AliYunAiApi aliYunAiApi;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;



    /**
     * @description 图片上传（URL）
     * @author RQ
     * @date 2025/7/11 下午7:38
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * @description 图片上传
     * @author RQ
     * @date 2025/6/12 下午2:53
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file") MultipartFile file,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * @description 图片删除
     * @author RQ
     * @date 2025/6/13 下午4:38
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest request) {
        // 1. 参数校验（使用ThrowUtils统一校验逻辑）
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 2. 获取登录用户（带登录态校验）
        User loginUser = userService.getLoginUser(request);
        // 3. 校验图片存在性（使用NOT_FOUND_ERROR更准确）
        Long id = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //已经改为使用注解鉴权
        // 4. 权限校验（管理员或资源所有者）
        //pictureService.checkSpaceAuth(oldPicture, loginUser);
        // 5. 执行删除并校验结果
         pictureService.deletePicture(id, loginUser);

        log.info("图片删除成功 ID:{} 操作者:{}", id, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * @description 更新图片（仅管理员可用）
     * @author RQ
     * @date 2025/6/13 下午5:00
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        // 1. 基础参数校验
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 2. 获取登录用户（带权限校验）
        User loginUser = userService.getLoginUser(request);
        // 3. 转换DTO到实体
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 处理标签列表转JSON字符串（保持与数据库格式一致）
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 4. 业务校验（包含权限校验）
        Picture oldPicture = pictureService.getById(picture.getId());

        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        pictureService.validPicture(picture);
        //4.补充审核参数
        pictureService.setPictureReviewStatus(picture, loginUser);
        // 6. 执行更新操作
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");
        pictureService.invalidatePictureDetailCache(picture.getId());

        log.info("图片更新成功 ID:{} 操作者:{}", picture.getId(), loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * @description 根据 id 获取图片（仅管理员可用）
     * @author RQ
     * @date 2025/6/13 下午5:08
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 查询缓存 / 数据库
        Picture picture = pictureService.getPictureByIdWithCache(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * @description 根据 id 获取图片（封装类）
     * @author RQ
     * @date 2025/6/13 下午5:09
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询缓存 / 数据库
        Picture picture = pictureService.getPictureByIdWithCache(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间的图片，需要校验权限
        Space space = null;
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 获取权限列表
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        pictureVO.setPermissionList(permissionList);
        // 获取封装类
        return ResultUtils.success(pictureVO);
    }

    /**
     * @description 分页获取图片列表（仅管理员可用）
     * @author RQ
     * @date 2025/6/13 下午5:18
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        //执行分页查询
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                //pictureService.getQueryWrapper()构建MyBatis-Plus查询条件
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * @description 分页获取图片列表（封装类）
     * @author RQ
     * @date 2025/6/13 下午6:16
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) { // 移除 request 参数
        // 参数校验（增强健壮性）
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || size <= 0, ErrorCode.PARAMS_ERROR, "分页参数错误");
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "单页数量不能超过20");
        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
// 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有/团队空间，通过 Sa-Token 校验权限
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
        }

        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取脱敏数据（包含关联用户信息）
        Page<PictureVO> voPage = pictureService.getPictureVOPage(picturePage);

        // 修改日志记录方式（移除登录用户依赖）
        log.info("图片分页查询成功 当前页:{}", current);
        return ResultUtils.success(voPage);
    }

    /**
     * @description 编辑图片(创建者或管理员)
     * @author RQ
     * @date 2025/6/13 下午6:58
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest,
                                             HttpServletRequest request) {
        // 1. 基础参数校验
        ThrowUtils.throwIf(pictureEditRequest == null || pictureEditRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 2. 转换DTO到实体
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 处理标签列表转JSON字符串
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 更新编辑时间
        picture.setEditTime(new Date());
        // 3. 业务规则校验
        User loginUser = userService.getLoginUser(request);
        Picture oldPicture = pictureService.getById(picture.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 权限校验：仅允许编辑自己的图片或管理员
        ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginUser.getId()) &&
                        !UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()),
                ErrorCode.NO_AUTH_ERROR);
        pictureService.validPicture(picture);
        //4.补充审核参数
        pictureService.setPictureReviewStatus(picture, loginUser);
        // 5. 将图片从草稿状态发布为正式图片
        picture.setIsDraft(0);
        //已经改为使用注解鉴权
        // 空间权限校验
//        pictureService.checkSpaceAuth(picture, loginUser);
        // 6. 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");
        pictureService.invalidatePictureDetailCache(picture.getId());

        log.info("图片编辑成功 ID:{} 操作者:{}", picture.getId(), loginUser.getId());
        return ResultUtils.success(true);
    }


    /**
     * @description 分页获取图片列表cache（封装类）
     * @author RQ
     * @date 2025/7/19 下午5:12
     */

    @Deprecated
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                      HttpServletRequest request) {
        // 调用 Service 层方法
        Page<PictureVO> pictureVOPage = pictureService.listPictureVOByPageWithCache(pictureQueryRequest, request);
        return ResultUtils.success(pictureVOPage);
    }


    /**
     * @description 图片审核
     * @author RQ
     * @date 2025/7/8 上午10:15
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null || pictureReviewRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        pictureService.invalidatePictureDetailCache(pictureReviewRequest.getId());

        return ResultUtils.success(true);
    }


    /**
     * @description 抓取图片
     * @author RQ
     * @date 2025/7/17 上午9:43
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> doPictureBatch(@RequestBody PictureByBatchRequest pictureByBatchRequest,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(pictureByBatchRequest == null || pictureByBatchRequest.getSearchText() == null,
                ErrorCode.PARAMS_ERROR, "请求参数非法");
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.doPictureBatchUpload(pictureByBatchRequest, loginUser);

        return ResultUtils.success(uploadCount);
    }


    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }
   /**
    * @description 创建 AI 扩图任务
    * @author RQ
    * @date 2025/9/24 下午1:48
    */
   @PostMapping("/out_painting/create_task")
   @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
   // 60 秒内最多 10 次，防止恶意刷接口
   @RateLimiter(key = "createOutPainting", time = 60, count = 10)
   public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
           @RequestBody(required = true) CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
           HttpServletRequest request) {
       // 1. 校验请求参数：确保请求体和图片 ID 不为空
       ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null,
               ErrorCode.PARAMS_ERROR, "请求参数不能为空");
       ThrowUtils.throwIf(createPictureOutPaintingTaskRequest.getPictureId() == null,
               ErrorCode.PARAMS_ERROR, "图片 ID 不能为空");

       // 2. 获取当前登录用户（权限校验）
       User loginUser = userService.getLoginUser(request);

       // 3. 调用业务服务层创建扩图任务
       CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(
               createPictureOutPaintingTaskRequest, loginUser);

       // 4. 返回成功响应
       return ResultUtils.success(response);
   }

    /**
     * @description 查询 AI 扩图任务
     * @author RQ
     * @date 2025/9/24 下午1:48
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        //1. 校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        // 2. 调用 AI 服务查询任务状态
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        // 3.校验任务是否存在
        ThrowUtils.throwIf(task == null, ErrorCode.PARAMS_ERROR, "任务不存在");
        return ResultUtils.success(task);
    }

}
