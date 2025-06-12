package com.RQ.tuyunthinktank.controller;

import com.RQ.tuyunthinktank.annotation.AuthCheck;
import com.RQ.tuyunthinktank.common.BaseResponse;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.constant.UserConstant;
import com.RQ.tuyunthinktank.model.dto.picture.PictureUploadRequest;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.enums.UserRoleEnum;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.RQ.tuyunthinktank.service.PictureService;
import com.RQ.tuyunthinktank.service.UserService;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
     * @description 图片上传
     * @author RQ
     * @date 2025/6/12 下午2:53
     */
    @PostMapping("/upload")
    @AuthCheck (mustRole= UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestParam("file") MultipartFile file,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest request)
    {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
}
