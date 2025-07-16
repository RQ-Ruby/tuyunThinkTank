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
import com.RQ.tuyunthinktank.model.dto.picture.*;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.enums.PictureReviewStatusEnum;
import com.RQ.tuyunthinktank.model.enums.UserRoleEnum;
import com.RQ.tuyunthinktank.model.vo.PictureTagCategory;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.RQ.tuyunthinktank.service.PictureService;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    /**
     * @description 图片上传（URL）
     * @author RQ
     * @date 2025/7/11 下午7:38
     */
    @PostMapping("/upload/url")
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
        // 4. 权限校验（管理员或资源所有者）
        if (!loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())
                && !oldPicture.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 5. 执行删除并校验结果
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片删除失败");

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
        // 查询数据库
        Picture picture = pictureService.getById(id);
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
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "请求参数非法");
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
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
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest) { // 移除 request 参数
        // 参数校验（增强健壮性）
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(current <= 0 || size <= 0, ErrorCode.PARAMS_ERROR, "分页参数错误");
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR, "单页数量不能超过20");
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

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
        // 5. 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片更新失败");

        log.info("图片编辑成功 ID:{} 操作者:{}", picture.getId(), loginUser.getId());
        return ResultUtils.success(true);
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

        return ResultUtils.success(true);
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

}
