package com.RQ.tuyunthinktank.manage.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @description 图片上传服务（URL模式）
 * @author RQ
 * @date 2025/7/16 下午7:32
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    // 常量定义（提高可维护性）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB限制
    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("jpeg", "jpg", "png", "webp"); // 允许的图片格式
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // ... 跟之前的校验逻辑保持一致
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仅支持HTTP/HTTPS协议");
        }
   /*     // 校验文件扩展名
        String fileExtension = FileUtil.getSuffix(fileUrl).toLowerCase();
        ThrowUtils.throwIf(!ALLOWED_EXTENSIONS.contains(fileExtension),
                ErrorCode.PARAMS_ERROR, "仅支持JPG/PNG/WEBP格式");
*/
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从 URL 中提取文件名
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}
