package com.RQ.tuyunthinktank.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 腾讯云cos配置
 */
@Configuration
// 配置文件前缀
// 注意：这里的前缀需要和application.yml中的前缀保持一致
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {

    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    /*腾讯云 Java SDK 源:https://cloud.tencent.com/document/product/436/65935*/
    public COSClient cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端
        return new COSClient(cred, clientConfig);
    }

    /**
     * 创建 TransferManager 实例，用于分片上传和断点续传
     * 源于: https://cloud.tencent.com/document/product/436/65935
     */
    @Bean(destroyMethod = "shutdownNow")
    public TransferManager transferManager(COSClient cosClient) {
        // 创建线程池用于分片上传的并发控制
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 设置高级接口的配置项
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        // 分块上传阈值设置为 5MB（超过此大小自动使用分块上传）
        transferManagerConfiguration.setMultipartUploadThreshold(5 * 1024 * 1024);
        // 分块大小设置为 1MB
        transferManagerConfiguration.setMinimumUploadPartSize(1 * 1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);
        return transferManager;
    }
}

