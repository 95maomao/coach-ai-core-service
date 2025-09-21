package com.coachai.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 */
@Configuration
@ConfigurationProperties(prefix = "oss")
@Data
public class OssConfig {

    /**
     * OSS服务端点
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 地域代码
     */
    private String region;

    /**
     * 是否使用HTTPS
     */
    private Boolean secure = true;

    /**
     * CDN域名（可选）
     */
    private String cdnDomain;

    /**
     * 自定义域名（可选）
     */
    private String customDomain;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout = 50000;

    /**
     * Socket超时时间（毫秒）
     */
    private Integer socketTimeout = 50000;

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
        private String maxFileSize = "20MB";

        /**
         * 最大请求大小
         */
        private String maxRequestSize = "20MB";
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
     * 创建OSS客户端Bean
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 获取文件访问的基础URL
     * 优先级：自定义域名 > CDN域名 > 默认域名
     */
    public String getBaseUrl() {
        if (customDomain != null && !customDomain.trim().isEmpty()) {
            return customDomain.endsWith("/") ? customDomain.substring(0, customDomain.length() - 1) : customDomain;
        }
        
        if (cdnDomain != null && !cdnDomain.trim().isEmpty()) {
            return cdnDomain.endsWith("/") ? cdnDomain.substring(0, cdnDomain.length() - 1) : cdnDomain;
        }
        
        // 默认使用OSS的外网域名
        return "https://" + bucketName + "." + endpoint.replace("https://", "").replace("http://", "");
    }
}
