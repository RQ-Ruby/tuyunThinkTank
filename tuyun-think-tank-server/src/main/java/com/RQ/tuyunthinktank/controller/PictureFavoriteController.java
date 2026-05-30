package com.RQ.tuyunthinktank.controller;

import com.RQ.tuyunthinktank.annotation.AuthCheck;
import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.model.dto.picture.PictureQueryRequest;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.RQ.tuyunthinktank.service.PictureFavoriteService;
import com.RQ.tuyunthinktank.service.PictureService;
import com.RQ.tuyunthinktank.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片收藏控制器
 */
@RestController
@RequestMapping("/picture/favorite")
@Slf4j
public class PictureFavoriteController {

    @Resource
    private PictureFavoriteService pictureFavoriteService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 收藏图片
     */
    @PostMapping("/add/{pictureId}")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Boolean> addFavorite(@PathVariable Long pictureId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        log.info("收藏图片请求，pictureId: {}, userId: {}", pictureId, loginUser.getId());
        
        Picture picture = pictureService.getById(pictureId);
        if (picture == null) {
            log.warn("图片不存在，pictureId: {}", pictureId);
            ThrowUtils.throwIf(true, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        
        log.info("图片信息 - isDraft: {}, reviewStatus: {}", picture.getIsDraft(), picture.getReviewStatus());
        
        // 检查图片是否为草稿
        ThrowUtils.throwIf(picture.getIsDraft() != null && picture.getIsDraft() == 1, 
                ErrorCode.OPERATION_ERROR, "草稿图片无法收藏");
        
        // 检查图片审核状态（只有审核通过的图片才能收藏）
        ThrowUtils.throwIf(picture.getReviewStatus() == null || picture.getReviewStatus() != 1, 
                ErrorCode.OPERATION_ERROR, "图片未通过审核，无法收藏");
        
        boolean result = pictureFavoriteService.addFavorite(pictureId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove/{pictureId}")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Boolean> removeFavorite(@PathVariable Long pictureId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        boolean result = pictureFavoriteService.removeFavorite(pictureId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{pictureId}")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Boolean> checkFavorite(@PathVariable Long pictureId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        boolean result = pictureFavoriteService.isFavorite(pictureId, loginUser.getId());
        return ResultUtils.success(result);
    }

    /**
     * 获取我的收藏列表（分页）
     */
    @PostMapping("/my/list/page")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Page<PictureVO>> listMyFavoritePictureByPage(
            @RequestBody PictureQueryRequest pictureQueryRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        
        QueryWrapper<com.RQ.tuyunthinktank.model.entity.PictureFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.orderByDesc("createTime");
        
        Page<com.RQ.tuyunthinktank.model.entity.PictureFavorite> favoritePage = 
            pictureFavoriteService.page(new Page<>(current, size), queryWrapper);
        
        List<Long> pictureIds = favoritePage.getRecords().stream()
                .map(com.RQ.tuyunthinktank.model.entity.PictureFavorite::getPictureId)
                .collect(Collectors.toList());
        
        if (pictureIds.isEmpty()) {
            return ResultUtils.success(new Page<>(current, size, 0));
        }
        
        List<Picture> pictures = pictureService.listByIds(pictureIds);
        List<PictureVO> pictureVOList = pictures.stream()
                .map(picture -> pictureService.getPictureVO(picture, request))
                .collect(Collectors.toList());
        
        Page<PictureVO> pictureVOPage = new Page<>(current, size, favoritePage.getTotal());
        pictureVOPage.setRecords(pictureVOList);
        
        return ResultUtils.success(pictureVOPage);
    }
}