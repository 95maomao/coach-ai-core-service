package com.coachai.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 上传图片文件
     *
     * @param file 图片文件
     * @return 文件访问URL
     */
    String uploadImage(MultipartFile file);

    /**
     * 上传文档文件
     *
     * @param file 文档文件
     * @return 文件访问URL
     */
    String uploadDocument(MultipartFile file);

    /**
     * 上传临时文件
     *
     * @param file 临时文件
     * @return 文件访问URL
     */
    String uploadTempFile(MultipartFile file);

    /**
     * 保存base64图片
     *
     * @param base64Image base64编码的图片数据
     * @return 文件访问URL
     */
    String saveBase64Image(String base64Image);

    /**
     * 从JSON字符串中保存base64图片
     *
     * @param jsonString 包含base64图片的JSON字符串
     * @return 文件访问URL
     */
    String saveBase64ImageFromJson(String jsonString);

    /**
     * 从URL下载图片并保存
     *
     * @param imageUrl 图片URL
     * @return 保存后的文件访问URL
     */
    String downloadAndSaveImage(String imageUrl);

    /**
     * 下载文件为字节数组
     *
     * @param objectName 对象名称
     * @return 文件字节数组
     */
    byte[] downloadFileAsBytes(String objectName);

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return 文件信息Map
     */
    Map<String, Object> getFileInfo(String objectName);

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String objectName);

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     */
    void deleteFile(String objectName);

    /**
     * 获取预签名URL
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（秒）
     * @return 预签名URL
     */
    String getPresignedUrl(String objectName, int expiry);
}
