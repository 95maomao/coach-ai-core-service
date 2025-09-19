package com.coachai.service;

import com.coachai.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 存储路径
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 确保存储桶存在
            ensureBucketExists();

            // 生成唯一文件名
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = path + fileName;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("文件上传成功: {}", objectName);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传图片文件
     *
     * @param file 图片文件
     * @return 图片访问URL
     */
    public String uploadImage(MultipartFile file) {
        // 验证文件类型
        if (!isImageFile(file)) {
            throw new IllegalArgumentException("不支持的文件类型，请上传图片文件");
        }
        return uploadFile(file, minioConfig.getPaths().getImages());
    }

    /**
     * 上传文档文件
     *
     * @param file 文档文件
     * @return 文档访问URL
     */
    public String uploadDocument(MultipartFile file) {
        return uploadFile(file, minioConfig.getPaths().getDocuments());
    }

    /**
     * 上传临时文件
     *
     * @param file 临时文件
     * @return 临时文件访问URL
     */
    public String uploadTempFile(MultipartFile file) {
        return uploadFile(file, minioConfig.getPaths().getTemp());
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件为字节数组
     *
     * @param objectName 对象名称
     * @return 文件字节数组
     */
    public byte[] downloadFileAsBytes(String objectName) {
        try (InputStream inputStream = downloadFile(objectName)) {
            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return 文件信息 Map
     */
    public Map<String, Object> getFileInfo(String objectName) {
        try {
            // 使用 statObject 获取文件信息
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            
            // 构建文件信息 Map
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("objectName", objectName);
            fileInfo.put("bucketName", minioConfig.getBucketName());
            fileInfo.put("exists", true);
            
            return fileInfo;
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", objectName, e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 生成预签名URL
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 预签名URL
     */
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(expiry)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", objectName, e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                log.info("创建存储桶: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("确保存储桶存在失败", e);
            throw new RuntimeException("确保存储桶存在失败: " + e.getMessage());
        }
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * 验证是否为图片文件
     *
     * @param file 文件
     * @return 是否为图片
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    private String getFileUrl(String objectName) {
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }
}
