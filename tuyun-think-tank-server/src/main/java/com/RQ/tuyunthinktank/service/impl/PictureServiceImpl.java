package com.RQ.tuyunthinktank.service.impl;

import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.manage.FileManage;
import com.RQ.tuyunthinktank.model.dto.file.UploadPictureResult;
import com.RQ.tuyunthinktank.model.dto.picture.PictureUploadRequest;
import com.RQ.tuyunthinktank.model.entity.User;
import com.RQ.tuyunthinktank.model.vo.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.RQ.tuyunthinktank.model.entity.Picture;
import com.RQ.tuyunthinktank.service.PictureService;
import com.RQ.tuyunthinktank.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author RQ
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-06-08 21:53:08
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Resource
    private FileManage fileManage;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //1.判断用户是否登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //2.初始化id,用于判断是更新还是新增
        Long id = null;
        if (pictureUploadRequest.getId() != null) {
            id = pictureUploadRequest.getId();
        }
        //3.如果是更新，判断是否存在该图片
        if (id != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, id)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        //4.按照用户id划分目录
        String originalFilename = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManage.uploadFile(multipartFile, originalFilename);
        //5.构造要入库的图片信息
        Picture picture = getPic(loginUser, uploadPictureResult, id);
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
        return PictureVO.objToVo(picture);


    }
/**
 * @description 构造要入库的图片信息
 * @author RQ
 * @date 2025/6/12 下午3:22
 */
    private static Picture getPic(User loginUser, UploadPictureResult uploadPictureResult, Long id) {
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        //6.如果 pictureId 不为空，表示更新，否则是新增
        if (id != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(id);
            picture.setEditTime(new Date());
        }
        return picture;
    }
}




