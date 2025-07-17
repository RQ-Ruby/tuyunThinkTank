package com.RQ.tuyunthinktank.manage.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.RQ.tuyunthinktank.config.CosClientConfig;
import com.RQ.tuyunthinktank.exception.BusinessException;
import com.RQ.tuyunthinktank.exception.ErrorCode;
import com.RQ.tuyunthinktank.manage.SaveManage;
import com.RQ.tuyunthinktank.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.RQ.tuyunthinktank.manage.upload.PictureUploadTemplate;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 图片上传模板类（使用模板方法模式）
 * 定义标准化的图片上传流程，具体实现由子类完成：
 * 1. 支持本地文件/网络图片等不同来源
 * 2. 自动生成唯一文件名
 * 3. 上传到对象存储
 * 4. 返回标准化结果
 *
 * @className: PictureUploadTemplate
 * @author/date: RQ/2025/07/16
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected SaveManage cosManager;  // 对象存储管理组件

    @Resource
    protected CosClientConfig cosClientConfig;  // COS客户端配置

    /**
     * 模板方法：定义标准上传流程（不可重写）
     *
     * @param inputSource 图片来源（由子类定义具体类型）
     * @param uploadPathPrefix 存储路径前缀（如：avatar/）
     * @return 标准化的上传结果
     * @throws BusinessException 上传失败时抛出业务异常
     */
    // 定义 Windows 系统不允许的特殊字符正则表达式
    private static final Pattern INVALID_FILE_NAME_CHARS = Pattern.compile("[<>:\"/\\\\|?*]");

    /**
     * 过滤文件名中的非法字符
     * @param filename 原始文件名
     * @return 过滤后的文件名
     */
    private String sanitizeFilename(String filename) {
        return INVALID_FILE_NAME_CHARS.matcher(filename).replaceAll("");
    }

    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片有效性（抽象方法）
        validPicture(inputSource);

        // 2. 生成唯一文件名：日期_uuid.后缀
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);
        // 过滤原始文件名中的非法字符
        originFilename = sanitizeFilename(originFilename);
        String uploadFilename = String.format("%s_%s.%s",
                DateUtil.formatDate(new Date()),
                uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);

        File tempFile = null;
        try {
            // 3. 创建临时文件（用于中转）
            tempFile = File.createTempFile(uploadPath, null);

            // 4. 处理输入源→写入临时文件（抽象方法）
            processFile(inputSource, tempFile);

            // 5. 上传到对象存储
            PutObjectResult putObjectResult = cosManager.putPicture(uploadPath, tempFile);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();

            // 6. 构建标准化返回结果
            return buildResult(originFilename, tempFile, uploadPath, imageInfo);
        } catch (Exception e) {
            log.error("图片上传失败 | path={} | error={}", uploadPath, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传服务异常");
        } finally {
            // 7. 清理临时文件（确保资源释放）
            deleteTempFile(tempFile);
        }
    }

    /**
     * 校验图片有效性（抽象方法）
     * 子类需实现具体校验逻辑：格式、大小、内容等
     *
     * @param inputSource 图片来源
     * @throws BusinessException 校验失败时抛出异常
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取原始文件名（抽象方法）
     *
     * @param inputSource 图片来源
     * @return 原始文件名（含后缀）
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源→写入临时文件（抽象方法）
     * 子类需实现具体转换逻辑：下载网络图片/复制本地文件等
     *
     * @param inputSource 图片来源
     * @param tempFile 目标临时文件
     * @throws Exception 处理失败时抛出异常
     */
    protected abstract void processFile(Object inputSource, File tempFile) throws Exception;

    /**
     * 构建标准化返回结果
     *
     * @param originFilename 原始文件名
     * @param file 临时文件对象
     * @param uploadPath 存储路径
     * @param imageInfo 图片元信息
     * @return 标准化的上传结果
     */
    private UploadPictureResult buildResult(String originFilename, File file,
                                            String uploadPath, ImageInfo imageInfo) {
        UploadPictureResult result = new UploadPictureResult();
        // 提取图片元信息
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double scale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();

        // 封装返回数据
        result.setPicName(FileUtil.mainName(originFilename));  // 不含后缀的文件名
        result.setPicWidth(width);
        result.setPicHeight(height);
        result.setPicScale(scale);
        result.setPicFormat(imageInfo.getFormat());
        result.setPicSize(FileUtil.size(file));
        result.setUrl(cosClientConfig.getHost() + "/" + uploadPath);  // 完整访问URL

        return result;
    }

    /**
     * 清理临时文件
     *
     * @param file 待删除的临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) return;

        if (!file.delete()) {  // 删除失败时记录告警
            log.warn("临时文件删除失败 | path={}", file.getAbsolutePath());
        }
    }
}