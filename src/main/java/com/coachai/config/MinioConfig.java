package com.coachai.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {

    /**
     * MinIO 服务端点
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 是否使用 HTTPS
     */
    private Boolean secure = false;

    /**
     * 文件上传配置
     */
    private UploadConfig upload = new UploadConfig();

    /**
     * 存储路径配置
     */
    private PathsConfig paths = new PathsConfig();

    @Data
    public static class UploadConfig {
        /**
         * 最大文件大小
         */
        private String maxFileSize = "10MB";

        /**
         * 最大请求大小
         */
        private String maxRequestSize = "10MB";
    }

    @Data
    public static class PathsConfig {
        /**
         * 图片存储路径
         */
        private String images = "images/";

        /**
         * 文档存储路径
         */
        private String documents = "documents/";

        /**
         * 临时文件存储路径
         */
        private String temp = "temp/";
    }

    /**
     * 创建 MinIO 客户端 Bean
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
