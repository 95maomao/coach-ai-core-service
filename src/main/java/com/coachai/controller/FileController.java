package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件管理控制器
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * 上传图片文件
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            String fileUrl = fileStorageService.uploadImage(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("fileName", file.getOriginalFilename());
            result.put("fileUrl", fileUrl);
            result.put("fileSize", String.valueOf(file.getSize()));
            result.put("contentType", file.getContentType());

            return ApiResponse.success("图片上传成功", result);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return ApiResponse.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传文档文件
     *
     * @param file 文档文件
     * @return 上传结果
     */
    @PostMapping("/upload/document")
    public ApiResponse<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            String fileUrl = fileStorageService.uploadDocument(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("fileName", file.getOriginalFilename());
            result.put("fileUrl", fileUrl);
            result.put("fileSize", String.valueOf(file.getSize()));
            result.put("contentType", file.getContentType());

            return ApiResponse.success("文档上传成功", result);
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return ApiResponse.error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传临时文件
     *
     * @param file 临时文件
     * @return 上传结果
     */
    @PostMapping("/upload/temp")
    public ApiResponse<Map<String, String>> uploadTempFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            String fileUrl = fileStorageService.uploadTempFile(file);
            
            Map<String, String> result = new HashMap<>();
            result.put("fileName", file.getOriginalFilename());
            result.put("fileUrl", fileUrl);
            result.put("fileSize", String.valueOf(file.getSize()));
            result.put("contentType", file.getContentType());

            return ApiResponse.success("临时文件上传成功", result);
        } catch (Exception e) {
            log.error("临时文件上传失败", e);
            return ApiResponse.error("临时文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称（从URL中提取）
     * @return 文件流
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("objectName") String objectName) {
        try {
            byte[] fileBytes = fileStorageService.downloadFileAsBytes(objectName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(fileBytes.length);
            headers.setContentDispositionFormData("attachment", objectName);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取文件信息
     *
     * @param objectName 对象名称
     * @return 文件信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getFileInfo(@RequestParam("objectName") String objectName) {
        try {
            Map<String, Object> result = fileStorageService.getFileInfo(objectName);
            return ApiResponse.success("获取文件信息成功", result);
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", objectName, e);
            return ApiResponse.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    @GetMapping("/exists")
    public ApiResponse<Boolean> fileExists(@RequestParam("objectName") String objectName) {
        try {
            boolean exists = fileStorageService.fileExists(objectName);
            return ApiResponse.success("检查文件存在性成功", exists);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", objectName, e);
            return ApiResponse.error("检查文件存在性失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> deleteFile(@RequestParam("objectName") String objectName) {
        try {
            fileStorageService.deleteFile(objectName);
            return ApiResponse.success("文件删除成功", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名URL
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（秒，默认3600秒）
     * @return 预签名URL
     */
    @GetMapping("/presigned-url")
    public ApiResponse<Map<String, String>> getPresignedUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiry", defaultValue = "3600") int expiry) {
        try {
            String presignedUrl = fileStorageService.getPresignedUrl(objectName, expiry);
            
            Map<String, String> result = new HashMap<>();
            result.put("objectName", objectName);
            result.put("presignedUrl", presignedUrl);
            result.put("expiry", String.valueOf(expiry));

            return ApiResponse.success("生成预签名URL成功", result);
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", objectName, e);
            return ApiResponse.error("生成预签名URL失败: " + e.getMessage());
        }
    }
}
