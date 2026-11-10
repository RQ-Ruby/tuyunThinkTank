package com.RQ.tuyunthinktank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 项目运行期配置。
 *
 * <p>除应用名、激活 profile、本地端口和 Nacos 引导信息外，
 * 其余可变配置统一从 Nacos 绑定到这里。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tuyun")
public class TuyunProperties {

    private Cors cors = new Cors();
    private WebSocket websocket = new WebSocket();
    private Upload upload = new Upload();
    private Cache cache = new Cache();
    private Security security = new Security();
    private Batch batch = new Batch();
    private RateLimit rateLimit = new RateLimit();
    private Ai ai = new Ai();

    @Data
    public static class Cors {
        private List<String> allowedOriginPatterns = Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        );
        private boolean allowCredentials = true;
    }

    @Data
    public static class WebSocket {
        private String path = "/ws/picture/edit";
        private List<String> allowedOrigins = Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        );
        private int disruptorBufferSize = 1024 * 256;
    }

    @Data
    public static class Upload {
        private long fileMaxSize = 20L * 1024 * 1024;
        private long urlMaxSize = 10L * 1024 * 1024;
        private List<String> allowedExtensions = Arrays.asList("jpeg", "jpg", "png", "webp");
    }

    @Data
    public static class Cache {
        private long localMaxSize = 1000L;
        private long localTtlMinutes = 5L;
        private long detailTtlSeconds = 3600L;
        private long detailTtlJitterSeconds = 300L;
        private long detailNullTtlSeconds = 300L;
        private long detailLockTtlSeconds = 3L;
        private int pageTtlMinSeconds = 300;
        private int pageTtlMaxSeconds = 600;
        private double bloomErrorRate = 0.001D;
        private long bloomCapacity = 1_000_000L;
        private long bloomInitLockTtlSeconds = 60L;
    }

    @Data
    public static class Security {
        private String passwordSalt = "RuQian";
    }

    @Data
    public static class Batch {
        private int maxFetchCount = 30;
        private int maxPageSize = 20;
    }

    @Data
    public static class RateLimit {
        private int windowSeconds = 60;
        private int count = 10;
    }

    @Data
    public static class Ai {
        private Chat chat = new Chat();
        private Embedding embedding = new Embedding();
        private OutPainting outPainting = new OutPainting();
    }

    @Data
    public static class Chat {
        private String baseUrl;
        private String apiKey;
        private String model = "deepseek-flash";
        private int maxTokens = 65536;
        private double temperature = 0.7D;
        private int contextWindow = 65536;
    }

    @Data
    public static class Embedding {
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String apiKey;
        private String model = "text-embedding-v4";
        private int dimensions = 1536;
    }

    @Data
    public static class OutPainting {
        private String apiKey;
        private String createTaskUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
        private String getTaskUrl = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";
    }
}
