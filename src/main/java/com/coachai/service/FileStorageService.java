package com.coachai.service;

import com.coachai.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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
     * 通过URL下载图片并保存到MinIO
     *
     * @param imageUrl 图片URL
     * @return 保存后的文件访问URL
     */
    public String downloadAndSaveImage(String imageUrl) {
        return downloadAndSaveFromUrl(imageUrl, minioConfig.getPaths().getImages());
    }

    /**
     * 将base64图片保存到MinIO
     *
     * @param base64Image base64编码的图片数据
     * @return 保存后的文件访问URL
     */
    public String saveBase64Image(String base64Image) {
        return saveBase64File(base64Image, minioConfig.getPaths().getImages());
    }

    /**
     * 从JSON字符串中解析并保存base64图片
     *
     * @param jsonString 包含base64数据的JSON字符串或直接的base64字符串
     * @return 保存后的文件访问URL
     */
    public String saveBase64ImageFromJson(String jsonString) {
        try {
            String base64Data = extractBase64FromJson(jsonString);
            return saveBase64File(base64Data, minioConfig.getPaths().getImages());
        } catch (Exception e) {
            log.error("从JSON字符串中解析base64数据失败", e);
            throw new RuntimeException("从JSON字符串中解析base64数据失败: " + e.getMessage());
        }
    }

    /**
     * 将base64文件保存到MinIO
     *
     * @param base64Data base64编码的文件数据
     * @param path       存储路径
     * @return 保存后的文件访问URL
     */
    public String saveBase64File(String base64Data, String path) {
        try {
            // 确保存储桶存在
            ensureBucketExists();

            // 解析base64数据
            String[] parts = parseBase64Data(base64Data);
            String mimeType = parts[0];
            String base64Content = parts[1];

            // 解码base64数据
            byte[] fileBytes = Base64.getDecoder().decode(base64Content);
            
            // 根据MIME类型确定文件扩展名
            String extension = getExtensionFromMimeType(mimeType);
            
            // 生成唯一文件名
            String fileName = generateFileNameWithExtension(extension);
            String objectName = path + fileName;

            // 上传文件到MinIO
            try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .stream(inputStream, fileBytes.length, -1)
                                .contentType(mimeType != null ? mimeType : "application/octet-stream")
                                .build()
                );
            }

            log.info("Base64文件保存成功: {} (大小: {} bytes)", objectName, fileBytes.length);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("Base64文件保存失败", e);
            throw new RuntimeException("Base64文件保存失败: " + e.getMessage());
        }
    }

    /**
     * 通过URL下载文件并保存到MinIO
     *
     * @param fileUrl 文件URL
     * @param path    存储路径
     * @return 保存后的文件访问URL
     */
    public String downloadAndSaveFromUrl(String fileUrl, String path) {
        try {
            // 确保存储桶存在
            ensureBucketExists();

            // 创建URL连接
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            
            // 设置请求头，模拟浏览器访问
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000); // 10秒连接超时
            connection.setReadTimeout(30000);    // 30秒读取超时

            // 获取文件信息
            String contentType = connection.getContentType();
            int contentLength = connection.getContentLength();
            
            // 从URL中提取文件扩展名，如果没有则根据Content-Type推断
            String extension = getFileExtensionFromUrl(fileUrl, contentType);
            
            // 生成唯一文件名
            String fileName = generateFileNameWithExtension(extension);
            String objectName = path + fileName;

            // 下载并上传文件
            try (InputStream inputStream = connection.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .stream(inputStream, contentLength, -1)
                                .contentType(contentType != null ? contentType : "application/octet-stream")
                                .build()
                );
            }

            log.info("从URL下载文件成功: {} -> {}", fileUrl, objectName);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", fileUrl, e);
            throw new RuntimeException("从URL下载文件失败: " + e.getMessage());
        }
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

    /**
     * 从URL和Content-Type中获取文件扩展名
     *
     * @param fileUrl     文件URL
     * @param contentType Content-Type
     * @return 文件扩展名
     */
    private String getFileExtensionFromUrl(String fileUrl, String contentType) {
        // 首先尝试从URL中提取扩展名
        if (fileUrl != null && fileUrl.contains(".")) {
            String urlExtension = fileUrl.substring(fileUrl.lastIndexOf("."));
            // 移除查询参数
            if (urlExtension.contains("?")) {
                urlExtension = urlExtension.substring(0, urlExtension.indexOf("?"));
            }
            if (urlExtension.length() <= 5) { // 合理的扩展名长度
                return urlExtension;
            }
        }

        // 如果URL中没有扩展名，根据Content-Type推断
        if (contentType != null) {
            switch (contentType.toLowerCase()) {
                case "image/jpeg":
                case "image/jpg":
                    return ".jpg";
                case "image/png":
                    return ".png";
                case "image/gif":
                    return ".gif";
                case "image/webp":
                    return ".webp";
                case "image/bmp":
                    return ".bmp";
                case "application/pdf":
                    return ".pdf";
                case "text/plain":
                    return ".txt";
                default:
                    return ".bin"; // 默认扩展名
            }
        }

        return ".bin"; // 默认扩展名
    }

    /**
     * 生成带扩展名的唯一文件名
     *
     * @param extension 文件扩展名
     * @return 唯一文件名
     */
    private String generateFileNameWithExtension(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * 解析base64数据，提取MIME类型和内容
     *
     * @param base64Data base64数据
     * @return [mimeType, base64Content]
     */
    private String[] parseBase64Data(String base64Data) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64数据不能为空");
        }

        String mimeType = null;
        String base64Content = base64Data.trim();

        // 检查是否包含data URL格式: data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEA...
        if (base64Content.startsWith("data:")) {
            int commaIndex = base64Content.indexOf(",");
            if (commaIndex == -1) {
                throw new IllegalArgumentException("无效的Base64数据格式");
            }

            String header = base64Content.substring(0, commaIndex);
            base64Content = base64Content.substring(commaIndex + 1);

            // 提取MIME类型: data:image/jpeg;base64 -> image/jpeg
            if (header.contains(":") && header.contains(";")) {
                mimeType = header.substring(header.indexOf(":") + 1, header.indexOf(";"));
            }
        }

        // 验证base64格式
        try {
            Base64.getDecoder().decode(base64Content);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的Base64编码格式: " + e.getMessage());
        }

        return new String[]{mimeType, base64Content};
    }

    /**
     * 根据MIME类型获取文件扩展名
     *
     * @param mimeType MIME类型
     * @return 文件扩展名
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".bin"; // 默认扩展名
        }

        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            case "image/bmp":
                return ".bmp";
            case "image/svg+xml":
                return ".svg";
            case "application/pdf":
                return ".pdf";
            case "text/plain":
                return ".txt";
            case "application/json":
                return ".json";
            case "application/xml":
            case "text/xml":
                return ".xml";
            default:
                return ".bin"; // 默认扩展名
        }
    }

    /**
     * 从JSON字符串中提取base64数据
     *
     * @param jsonString JSON字符串或直接的base64字符串
     * @return base64字符串
     */
    private String extractBase64FromJson(String jsonString) {
        try {
            if (jsonString == null || jsonString.trim().isEmpty()) {
                throw new IllegalArgumentException("输入数据不能为空");
            }

            String trimmedInput = jsonString.trim();
            
            // 如果不是以 { 开头，可能是直接的base64字符串
            if (!trimmedInput.startsWith("{")) {
                log.info("输入似乎是直接的base64字符串，长度: {}", trimmedInput.length());
                return trimmedInput;
            }

            log.debug("解析JSON字符串: {}", trimmedInput.length() > 200 ? 
                trimmedInput.substring(0, 200) + "..." : trimmedInput);

            // 使用JSONPath尝试多种可能的路径来提取base64数据
            String[] possiblePaths = {
                "$.data.response.candidates[0].content.parts[0].inlineData.data",  // 你提供的路径
                "$.data.response.candidates[*].content.parts[*].inlineData.data",  // 通配符版本
                "$.response.candidates[0].content.parts[0].inlineData.data",       // 没有外层data的版本
                "$.candidates[0].content.parts[0].inlineData.data",                // 更简化的版本
                "$..inlineData.data",                                              // 递归搜索inlineData.data
                "$..data",                                                         // 递归搜索所有data字段
                "$.data"                                                           // 直接取data字段
            };

            for (String path : possiblePaths) {
                try {
                    Object result = JsonPath.read(trimmedInput, path);
                    if (result != null) {
                        String base64Data = null;
                        
                        if (result instanceof String) {
                            base64Data = (String) result;
                        } else if (result instanceof List) {
                            List<?> list = (List<?>) result;
                            if (!list.isEmpty() && list.get(0) instanceof String) {
                                base64Data = (String) list.get(0);
                            }
                        }
                        
                        if (base64Data != null && !base64Data.trim().isEmpty()) {
                            log.info("成功使用路径 {} 提取到base64数据，长度: {}", path, base64Data.length());
                            return base64Data;
                        }
                    }
                } catch (Exception e) {
                    log.debug("路径 {} 解析失败: {}", path, e.getMessage());
                    // 继续尝试下一个路径
                }
            }

            throw new IllegalArgumentException("无法从JSON中提取base64数据，请检查JSON结构是否正确");

        } catch (Exception e) {
            log.error("解析JSON数据时发生错误", e);
            throw new RuntimeException("解析JSON数据失败: " + e.getMessage());
        }
    }
}
