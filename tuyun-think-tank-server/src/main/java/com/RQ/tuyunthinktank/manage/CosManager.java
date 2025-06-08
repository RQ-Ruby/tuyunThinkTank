package com.RQ.tuyunthinktank.manage;

import com.RQ.tuyunthinktank.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author RQ
 * @description 腾讯云对象存储管理类（提供通用的对象存储操作）
 * @date 2025/5/25 下午4:26
 */
@Component
public class CosManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private COSClient cosClient;

    /**
     * @description 上传文件
     * @param key 存储在腾讯云cos的key
     * @param file 要上传的文件
     * @return com.qcloud.cos.model.PutObjectResult
     * @author RQ
     * @date 2025/6/8 下午4:07
     */
    /*上传对象 源于:https://cloud.tencent.com/document/product/436/65935*/
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

}
