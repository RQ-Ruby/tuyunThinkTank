package com.RQ.tuyunthinktank.manage;

import com.RQ.tuyunthinktank.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author RQ
 * @description 腾讯云对象存储管理类（提供通用的对象存储操作）
 * 用于存放通用的对象存储操作，如上传、下载、删除等
 * @date 2025/5/25 下午4:26
 */
@Component
public class SaveManage {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * @param key  存储在腾讯云cos的key
     * @param file 要上传的文件
     * @return com.qcloud.cos.model.PutObjectResult
     * @description 上传文件
     * @author RQ
     * @date 2025/6/8 下午4:07
     */
    /*上传对象 源于:https://cloud.tencent.com/document/product/436/65935*/
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * @param key  存储在腾讯云cos的key
     * @param file 待上传的本地文件对象
     * @return com.qcloud.cos.model.PutObjectResult
     * @description 上传图片
     * @author RQ
     * @date 2025/6/8 下午4:07
     */
    public PutObjectResult putPicture(String key, File file) {
        // 1. 创建上传请求对象
        // 参数说明：
        // - cosClientConfig.getBucket(): 从配置获取存储桶名称（格式：BucketName-APPID）
        // - key: 对象键（COS中的文件路径）
        // - file: 本地文件对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                cosClientConfig.getBucket(),
                key,
                file);

        // 2. 图片处理配置（文档来源：腾讯云图片处理API）
        /* 源于:https://cloud.tencent.com/document/product/436/55377 */
        PicOperations picOperations = new PicOperations();
        // 设置返回原图信息（1-返回，0-不返回）
        picOperations.setIsPicInfo(1);
        // 3. 将图片处理配置加入上传请求
        putObjectRequest.setPicOperations(picOperations);
        // 4. 执行上传操作并返回结果
        return cosClient.putObject(putObjectRequest);
    }


}
