package com.RQ.tuyunthinktank.manage.upload;

import cn.hutool.core.io.FileUtil;
import com.RQ.tuyunthinktank.config.TuyunProperties;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author RQ
 * @description 图片上传服务（文件模式）
 * @date 2025/7/16 下午7:32
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    @Resource
    private TuyunProperties tuyunProperties;

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;

        // 1. 空文件校验
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(),
                ErrorCode.PARAMS_ERROR, "上传文件不能为空");

        // 2. 文件大小校验
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > tuyunProperties.getUpload().getFileMaxSize(),
                ErrorCode.PARAMS_ERROR, "文件大小不能超过20MB");

        // 3. 文件格式校验
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = FileUtil.getSuffix(originalFilename).toLowerCase();
        ThrowUtils.throwIf(!tuyunProperties.getUpload().getAllowedExtensions().contains(fileExtension),
                ErrorCode.PARAMS_ERROR, "仅支持JPG/PNG/WEBP格式");
    }

    /**
     * @description 获取原始文件名（含后缀）
     * @author RQ
     * @date 2025/7/16 下午5:42
     */
    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    /**
     * @description 文件处理：将MultipartFile写入临时文件
     * @author RQ
     * @date 2025/7/16 下午5:42
     */
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
