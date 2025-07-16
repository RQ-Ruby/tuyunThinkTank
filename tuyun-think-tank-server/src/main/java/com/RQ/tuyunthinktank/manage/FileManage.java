package com.RQ.tuyunthinktank.manage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import com.RQ.tuyunthinktank.common.ResultUtils;
import com.RQ.tuyunthinktank.config.CosClientConfig;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.exception.ThrowUtils;
import com.RQ.tuyunthinktank.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

/**
 * @author RQ
 * @description 通用的图片校验和图片解析类
 * 用于存放通用的图片校验和图片解析操作，如校验图片格式、解析图片信息等
 * 注意：本类只提供通用的图片校验和图片解析操作，不提供图片上传操作
 * 图片上传操作请使用SaveManage类
 * @date 2025/5/25 下午4:26
 */
@Component
@Slf4j
//@Deprecated  已弃用
@Deprecated
public class FileManage {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    @Resource
    private SaveManage saveManage;

    /**
     * @param multipartFile  上传的文件
     * @param filePathPrefix 上传的文件路径
     * @return UploadPictureResult
     * @description 上传文件
     * @author RQ
     * @date 2025/6/11 下午10:43
     */

    public UploadPictureResult uploadFile(MultipartFile multipartFile, String filePathPrefix){
        //校验图片
        validateFile(multipartFile);
        //获取文件后缀名
        String contentType =  multipartFile.getOriginalFilename() != null ?  multipartFile.getOriginalFilename() : ""; // 避免空指针异常
        contentType = contentType.substring(contentType.lastIndexOf(".") + 1).toLowerCase();
        // 生成短UUID（取前8位）+日期，如 "20240612_a1b2c3d4.jpg"
        //UUID版本4，是一种随机生成的标识符，通常用于生成全局唯一的标识符，如数据库主键、文件名称等
        String shortUUID = UUID.randomUUID().toString().substring(0, 16);
        String safeFilename = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                shortUUID,
                contentType);
        //组合成完整的文件路径
        String yunFilePath ="tuyun-thinkTank";
        String filePath = String.format(yunFilePath+"/%s/%s", filePathPrefix, safeFilename);
        File temporaryFile = null;
        try {
            // 上传文件
            temporaryFile = File.createTempFile(filePath, null);
            multipartFile.transferTo(temporaryFile);
            //上传结果对象，用于后期解析图片信息
            PutObjectResult putObjectResult = saveManage.putPicture(filePath, temporaryFile);
            //获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
            // 图片信息封装
            uploadPictureResult.setPicName(FileUtil.mainName(multipartFile.getOriginalFilename()));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(temporaryFile));
            return uploadPictureResult;

        } catch (Exception e) {
            log.error("图片上传到对象存储失败, filepath = " + filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 删除临时文件
            deleteTemporaryFile(temporaryFile);
        }

    }


    /**
     * @param file
     * @return void
     * @description 校验文件
     * @author RQ
     * @date 2025/6/11 下午10:04
     */
    private void validateFile(MultipartFile file)  {
        //校验文件是否为空
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        //校验文件大小
        final long size = 10 * 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > size, ErrorCode.PARAMS_ERROR, "文件大小不能超过10MB");
        // 使用Apache Tika检测真实文件类型
        // 内容检测 确保上传文件是真实的图片类型，避免伪造风险
        try (InputStream is = file.getInputStream()) {
            String realType = new Tika().detect(is);
            ThrowUtils.throwIf(!realType.startsWith("image/"), ErrorCode.PARAMS_ERROR, "非图片文件");
        } catch (IOException e) {
            throw new RuntimeException("文件检测失败", e);
        }
        //校验文件格式
        String contentType = file.getOriginalFilename() != null ? file.getOriginalFilename() : ""; // 避免空指针异常
        contentType = contentType.substring(contentType.lastIndexOf(".") + 1).toLowerCase();
        //运行上传的图片后缀jpg png jpeg bmp
        final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "bmp");
        ThrowUtils.throwIf(!ALLOWED_EXTENSIONS.contains(contentType),
                ErrorCode.PARAMS_ERROR, "文件格式不正确");
    }

    /**
     * @description 删除临时文件
     * @author RQ
     * @date 2025/6/12 下午12:30
     */
    public static void deleteTemporaryFile(File temporaryFile) {
        if (temporaryFile != null) {
            // 删除临时文件
            boolean delete = temporaryFile.delete();
            if (!delete) {
                log.error("删除文件失败, filepath = {}", temporaryFile.getAbsolutePath());
            }
        }
    }
}
