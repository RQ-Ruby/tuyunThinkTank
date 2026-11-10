package com.RQ.tuyunthinktank.manage.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.RQ.tuyunthinktank.config.TuyunProperties;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author RQ
 * @description 图片上传服务（URL模式）
 * @date 2025/7/16 下午7:32
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Resource
    private TuyunProperties tuyunProperties;

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
        ThrowUtils.throwIf(file.length() > tuyunProperties.getUpload().getUrlMaxSize(),
                ErrorCode.PARAMS_ERROR, "远程图片大小超过限制");
    }
}
