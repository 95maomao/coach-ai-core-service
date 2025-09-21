package com.coachai.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.model.*;
import com.coachai.config.OssConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 阿里云OSS文件存储服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OssFileStorageService implements FileStorageService {

    private final OSS ossClient;
    private final OssConfig ossConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            if (!isImageFile(file)) {
                throw new IllegalArgumentException("文件类型不支持，仅支持图片文件");
            }

            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = ossConfig.getPaths().getImages() + fileName;

            // 创建上传请求
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            
            // 确保图片文件有正确的Content-Type
            String contentType = file.getContentType();
            if (contentType == null || contentType.equals("application/octet-stream")) {
                contentType = getContentTypeByExtension(fileName);
            }
            metadata.setContentType(contentType);
            
            metadata.setCacheControl("public, max-age=31536000"); // 1年缓存
            // 设置为inline，让浏览器直接显示而不是下载
            metadata.setContentDisposition("inline");

            // 记录元数据信息用于调试
            log.info("上传图片元数据: fileName={}, contentType={}, contentDisposition={}, size={}", 
                    fileName, contentType, "inline", file.getSize());

            // 上传文件
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    file.getInputStream(),
                    metadata
            );

            ossClient.putObject(putRequest);

            log.info("图片上传成功: {}", objectName);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new RuntimeException("图片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadDocument(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = ossConfig.getPaths().getDocuments() + fileName;

            // 创建上传请求
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            
            // 确保文件有正确的Content-Type
            String contentType = file.getContentType();
            if (contentType == null || contentType.equals("application/octet-stream")) {
                contentType = getContentTypeByExtension(fileName);
            }
            metadata.setContentType(contentType);
            
            // 设置为inline，让浏览器直接显示而不是下载
            metadata.setContentDisposition("inline");

            // 上传文件
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    file.getInputStream(),
                    metadata
            );

            ossClient.putObject(putRequest);

            log.info("文档上传成功: {}", objectName);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("文档上传失败", e);
            throw new RuntimeException("文档上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadTempFile(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String objectName = ossConfig.getPaths().getTemp() + fileName;

            // 创建上传请求
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            
            // 确保文件有正确的Content-Type
            String contentType = file.getContentType();
            if (contentType == null || contentType.equals("application/octet-stream")) {
                contentType = getContentTypeByExtension(fileName);
            }
            metadata.setContentType(contentType);
            
            // 临时文件设置较短的缓存时间
            metadata.setCacheControl("public, max-age=3600"); // 1小时缓存
            // 设置为inline，让浏览器直接显示而不是下载
            metadata.setContentDisposition("inline");

            // 上传文件
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    file.getInputStream(),
                    metadata
            );

            ossClient.putObject(putRequest);

            log.info("临时文件上传成功: {}", objectName);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("临时文件上传失败", e);
            throw new RuntimeException("临时文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String saveBase64Image(String base64Image) {
        try {
            log.info("开始保存Base64图片");

            // 解析base64数据
            String[] base64Parts = parseBase64Data(base64Image);
            String mimeType = base64Parts[0];
            String base64Content = base64Parts[1];

            // 解码base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Content);

            // 生成文件名
            String extension = getExtensionFromMimeType(mimeType);
            String fileName = generateFileNameWithExtension(extension);
            String objectName = ossConfig.getPaths().getImages() + fileName;

            // 创建上传请求
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType(mimeType);
            metadata.setCacheControl("public, max-age=31536000"); // 1年缓存
            // 设置为inline，让浏览器直接显示而不是下载
            metadata.setContentDisposition("inline");

            // 上传文件
            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    inputStream,
                    metadata
            );

            ossClient.putObject(putRequest);

            log.info("Base64图片保存成功: {} (大小: {} bytes)", objectName, imageBytes.length);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("Base64图片保存失败", e);
            throw new RuntimeException("Base64图片保存失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String saveBase64ImageFromJson(String jsonString) {
        try {
            log.info("开始从JSON保存Base64图片");

            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            
            // 尝试多种可能的JSON结构来查找base64图片数据
            String base64Image = null;
            String mimeType = "image/png"; // 默认MIME类型
            
            // 方式1: 查找 data.response.candidates[0].content.parts[].inlineData.data 结构
            JsonNode dataNode = jsonNode.get("data");
            if (dataNode != null) {
                JsonNode responseNode = dataNode.get("response");
                if (responseNode != null) {
                    JsonNode candidatesNode = responseNode.get("candidates");
                    if (candidatesNode != null && candidatesNode.isArray() && candidatesNode.size() > 0) {
                        JsonNode contentNode = candidatesNode.get(0).get("content");
                        if (contentNode != null) {
                            JsonNode partsNode = contentNode.get("parts");
                            if (partsNode != null && partsNode.isArray()) {
                                // 遍历parts数组，查找包含inlineData的部分
                                for (JsonNode part : partsNode) {
                                    JsonNode inlineDataNode = part.get("inlineData");
                                    if (inlineDataNode != null) {
                                        JsonNode dataField = inlineDataNode.get("data");
                                        JsonNode mimeTypeField = inlineDataNode.get("mimeType");
                                        if (dataField != null) {
                                            base64Image = dataField.asText();
                                            if (mimeTypeField != null) {
                                                mimeType = mimeTypeField.asText();
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 方式2: 查找简单的 image 字段（兼容旧格式）
            if (base64Image == null) {
                JsonNode imageNode = jsonNode.get("image");
                if (imageNode != null) {
                    base64Image = imageNode.asText();
                }
            }
            
            // 方式3: 查找 base64 字段
            if (base64Image == null) {
                JsonNode base64Node = jsonNode.get("base64");
                if (base64Node != null) {
                    base64Image = base64Node.asText();
                }
            }

            if (base64Image == null || base64Image.trim().isEmpty()) {
                throw new IllegalArgumentException("JSON中未找到有效的base64图片数据。支持的路径: data.response.candidates[0].content.parts[].inlineData.data, image, base64");
            }

            log.info("找到base64图片数据，MIME类型: {}, 数据长度: {}", mimeType, base64Image.length());
            
            // 如果base64数据没有MIME类型前缀，根据检测到的mimeType添加
            if (!base64Image.startsWith("data:")) {
                base64Image = "data:" + mimeType + ";base64," + base64Image;
            }
            
            return saveBase64Image(base64Image);

        } catch (Exception e) {
            log.error("从JSON保存Base64图片失败", e);
            throw new RuntimeException("从JSON保存Base64图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String downloadAndSaveImage(String imageUrl) {
        try {
            log.info("开始从URL下载图片: {}", imageUrl);

            // 下载图片
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "CoachAI/1.0");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("下载图片失败，HTTP状态码: " + connection.getResponseCode());
            }

            // 读取图片数据
            byte[] imageBytes = IOUtils.toByteArray(connection.getInputStream());
            String contentType = connection.getContentType();

            // 生成文件名
            String extension = getFileExtensionFromUrl(imageUrl, contentType);
            String fileName = generateFileNameWithExtension(extension);
            String objectName = ossConfig.getPaths().getImages() + fileName;

            // 创建上传请求
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            
            // 确保图片有正确的Content-Type
            if (contentType == null || contentType.equals("application/octet-stream")) {
                contentType = getContentTypeByExtension(fileName);
            }
            metadata.setContentType(contentType);
            
            metadata.setCacheControl("public, max-age=31536000"); // 1年缓存
            // 设置为inline，让浏览器直接显示而不是下载
            metadata.setContentDisposition("inline");

            // 上传到OSS
            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            PutObjectRequest putRequest = new PutObjectRequest(
                    ossConfig.getBucketName(),
                    objectName,
                    inputStream,
                    metadata
            );

            ossClient.putObject(putRequest);

            log.info("从URL下载图片成功: {} -> {}", imageUrl, objectName);
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("从URL下载图片失败: {}", imageUrl, e);
            throw new RuntimeException("从URL下载图片失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] downloadFileAsBytes(String objectName) {
        try {
            log.info("开始下载文件: {}", objectName);

            // 下载文件
            OSSObject ossObject = ossClient.getObject(ossConfig.getBucketName(), objectName);
            byte[] fileBytes = IOUtils.toByteArray(ossObject.getObjectContent());

            log.info("文件下载成功: {} (大小: {} bytes)", objectName, fileBytes.length);
            return fileBytes;

        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String objectName) {
        try {
            log.info("获取文件信息: {}", objectName);

            // 获取文件元数据
            ObjectMetadata metadata = ossClient.getObjectMetadata(ossConfig.getBucketName(), objectName);

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("objectName", objectName);
            fileInfo.put("size", metadata.getContentLength());
            fileInfo.put("contentType", metadata.getContentType());
            fileInfo.put("lastModified", metadata.getLastModified());
            fileInfo.put("etag", metadata.getETag());
            fileInfo.put("url", getFileUrl(objectName));

            log.info("获取文件信息成功: {}", objectName);
            return fileInfo;

        } catch (Exception e) {
            log.error("获取文件信息失败: {}", objectName, e);
            throw new RuntimeException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            return ossClient.doesObjectExist(ossConfig.getBucketName(), objectName);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", objectName, e);
            return false;
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            log.info("开始删除文件: {}", objectName);

            ossClient.deleteObject(ossConfig.getBucketName(), objectName);

            log.info("文件删除成功: {}", objectName);

        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new RuntimeException("文件删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrl(String objectName, int expiry) {
        try {
            log.info("生成预签名URL: {}, 过期时间: {}秒", objectName, expiry);

            // 生成预签名URL
            Date expiration = new Date(System.currentTimeMillis() + expiry * 1000L);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossConfig.getBucketName(), objectName, HttpMethod.GET);
            request.setExpiration(expiration);

            URL presignedUrl = ossClient.generatePresignedUrl(request);

            log.info("预签名URL生成成功: {}", objectName);
            return presignedUrl.toString();

        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", objectName, e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成唯一文件名
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
     * 生成带扩展名的唯一文件名
     */
    private String generateFileNameWithExtension(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * 验证是否为图片文件
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 获取文件访问URL
     * 返回阿里云OSS的直接访问URL
     */
    private String getFileUrl(String objectName) {
        // 使用OSS配置中的基础URL生成完整的文件访问URL
        // 格式: https://bucket-name.oss-region.aliyuncs.com/path/filename.jpg
        return ossConfig.getBaseUrl() + "/" + objectName;
    }

    /**
     * 解析base64数据，提取MIME类型和内容
     */
    private String[] parseBase64Data(String base64Data) {
        if (base64Data.startsWith("data:")) {
            // 格式: data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...
            int commaIndex = base64Data.indexOf(",");
            if (commaIndex != -1) {
                String header = base64Data.substring(5, commaIndex); // 去掉"data:"
                String content = base64Data.substring(commaIndex + 1);

                // 提取MIME类型
                String mimeType = "image/jpeg"; // 默认值
                if (header.contains(";")) {
                    mimeType = header.substring(0, header.indexOf(";"));
                }

                return new String[]{mimeType, content};
            }
        }

        // 如果不是标准格式，假设是纯base64内容
        return new String[]{"image/jpeg", base64Data};
    }

    /**
     * 根据MIME类型获取文件扩展名
     */
    private String getExtensionFromMimeType(String mimeType) {
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
            default:
                return ".jpg"; // 默认扩展名
        }
    }

    /**
     * 从URL和Content-Type中获取文件扩展名
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
            return getExtensionFromMimeType(contentType);
        }

        return ".bin"; // 默认扩展名
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentTypeByExtension(String fileName) {
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String extension = "";
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        }
        
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            case ".bmp":
                return "image/bmp";
            case ".svg":
                return "image/svg+xml";
            case ".pdf":
                return "application/pdf";
            case ".txt":
                return "text/plain";
            case ".html":
                return "text/html";
            case ".css":
                return "text/css";
            case ".js":
                return "application/javascript";
            default:
                return "application/octet-stream";
        }
    }
}