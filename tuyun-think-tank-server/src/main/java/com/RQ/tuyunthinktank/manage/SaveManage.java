package com.RQ.tuyunthinktank.manage;

import cn.hutool.core.io.FileUtil;
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
import java.util.ArrayList;
import java.util.List;

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
        //图像格式转换
        /* 源于:https://cloud.tencent.com/document/product/436/113299*/
        //创建图片处理规则
        List<PicOperations.Rule> picOperationList = new ArrayList<>();
        // 添加图片处理规则,将图片转换成webp格式
        //生成新的文件名
        String newKey = FileUtil.mainName(key) + ".webp";
        // 1.添加图片处理规则,将图片转换成webp格式
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setRule("imageMogr2/format/webp");
        //存储桶
        rule.setBucket(cosClientConfig.getBucket());
        // 设置新的文件名
        rule.setFileId(newKey);
        // 添加规则到列表
        picOperationList.add(rule);
        // 将规则列表绑定到图片处理配置
        picOperations.setRules(picOperationList);
        /*源:https://cloud.tencent.com/document/product/436/55377*/
        //仅对大于20KB的图片进行处理
        // 2. 生成缩略图
        // 缩略图处理，仅对 > 30 KB 的图片生成缩略图
        if (file.length() > 30 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            // 关键优化点1：强制使用WebP格式（减少30%体积，更清晰）
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail.webp";
            thumbnailRule.setFileId(thumbnailKey);

            // 关键优化点2：添加锐化+抗锯齿参数 | 优化缩放策略
            String r = String.format(
                    "imageMogr2/thumbnail/%sx%s>/strip/1/format/webp/sharp/100", 400, 400
            );
            thumbnailRule.setRule(r);
            picOperationList.add(thumbnailRule);
        }

        // 3. 将图片处理配置加入上传请求
        putObjectRequest.setPicOperations(picOperations);
        // 4. 执行上传操作并返回结果
        return cosClient.putObject(putObjectRequest);
    }


}
