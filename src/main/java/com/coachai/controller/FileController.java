package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.dto.Base64ImageRequest;
import com.coachai.service.FileStorageService;
import com.coachai.service.ImageCompressionService;
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
    private final ImageCompressionService imageCompressionService;

    /**
     * 上传图片文件
     *
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("接收到图片上传请求: fileName={}, size={}, contentType={}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
            
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
     * 通过URL下载图片并保存到MinIO
     *
     * @param imageUrl 图片URL
     * @return 保存结果
     */
    @PostMapping("/download/image")
    public ApiResponse<Map<String, String>> downloadImageFromUrl(@RequestParam("imageUrl") String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ApiResponse.error("图片URL不能为空");
            }

            String savedFileUrl = fileStorageService.downloadAndSaveImage(imageUrl);
            
            Map<String, String> result = new HashMap<>();
            result.put("originalUrl", imageUrl);
            result.put("savedFileUrl", savedFileUrl);
            result.put("message", "图片下载并保存成功");

            return ApiResponse.success("图片下载并保存成功", result);
        } catch (Exception e) {
            log.error("图片下载并保存失败: {}", imageUrl, e);
            return ApiResponse.error("图片下载并保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存base64图片到MinIO (通过请求参数，适用于小数据)
     *
     * @param base64Image base64编码的图片数据
     * @return 保存结果
     */
    @PostMapping("/upload/base64")
    public ApiResponse<Map<String, String>> uploadBase64Image(@RequestParam("base64Image") String base64Image) {
        try {
            if (base64Image == null || base64Image.trim().isEmpty()) {
                return ApiResponse.error("Base64图片数据不能为空");
            }
            
            // 优化日志输出，避免打印完整的base64数据
            String logData = base64Image.length() > 100 ? 
                base64Image.substring(0, 100) + "... (长度: " + base64Image.length() + ")" : base64Image;
            log.info("上传base64图片: {}", logData);
            
            String savedFileUrl = fileStorageService.saveBase64Image(base64Image);
            
            // 计算原始数据大小（估算）
            String base64Content = base64Image;
            if (base64Image.contains(",")) {
                base64Content = base64Image.substring(base64Image.indexOf(",") + 1);
            }
            int estimatedSize = (int) (base64Content.length() * 0.75); // base64解码后大约是原长度的75%
            
            Map<String, String> result = new HashMap<>();
            result.put("savedFileUrl", savedFileUrl);
            result.put("estimatedSize", String.valueOf(estimatedSize));
            result.put("message", "Base64图片保存成功");

            return ApiResponse.success("Base64图片保存成功", result);
        } catch (Exception e) {
            log.error("Base64图片保存失败", e);
            return ApiResponse.error("Base64图片保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存base64图片到MinIO (通过JSON请求体，适用于大数据)
     *
     * @param request base64图片请求对象
     * @return 保存结果
     */
    @PostMapping("/upload/base64-json")
    public ApiResponse<Map<String, String>> uploadBase64ImageJson(@RequestBody Base64ImageRequest request) {
        try {
            if (request == null || request.getBase64Image() == null || request.getBase64Image().trim().isEmpty()) {
                return ApiResponse.error("Base64图片数据不能为空");
            }
            
            String base64ImageData = request.getBase64Image();
            
            // 优化日志输出，避免打印完整的数据
            String logData = base64ImageData.length() > 100 ? 
                base64ImageData.substring(0, 100) + "... (字符串长度: " + base64ImageData.length() + ")" : base64ImageData;
            log.info("上传base64图片(JSON): {}, 文件名提示: {}", logData, request.getFileName());
            
            // 判断是直接的base64字符串还是JSON字符串
            String savedFileUrl;
            if (base64ImageData.trim().startsWith("{")) {
                // 如果以 { 开头，认为是JSON字符串，使用JSON解析方法
                savedFileUrl = fileStorageService.saveBase64ImageFromJson(base64ImageData);
            } else {
                // 否则认为是直接的base64字符串
                savedFileUrl = fileStorageService.saveBase64Image(base64ImageData);
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("savedFileUrl", savedFileUrl);
            result.put("originalFileName", request.getFileName());
            result.put("description", request.getDescription());
            result.put("message", "Base64图片保存成功");

            return ApiResponse.success("Base64图片保存成功", result);
        } catch (Exception e) {
            log.error("Base64图片保存失败", e);
            return ApiResponse.error("Base64图片保存失败: " + e.getMessage());
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

    /**
     * 图片代理访问接口
     * 通过应用服务器代理OSS图片，设置正确的Content-Disposition
     *
     * @param objectName 对象名称（如：images/20250921123044_dd060b89.png）
     * @return 图片数据流
     */
    @GetMapping("/proxy/{path:.+}")
    public ResponseEntity<byte[]> proxyImage(@PathVariable("path") String objectName) {
        try {
            log.info("代理访问图片: {}", objectName);
            
            // 从OSS下载文件
            byte[] imageBytes = fileStorageService.downloadFileAsBytes(objectName);
            
            // 根据文件扩展名设置Content-Type
            String contentType = getContentTypeByExtension(objectName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("public, max-age=31536000"); // 1年缓存
            // 设置为inline，让浏览器直接显示
            headers.set("Content-Disposition", "inline");
            
            log.info("代理返回图片: {}, Content-Type: {}, 大小: {} bytes", 
                    objectName, contentType, imageBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageBytes);
                    
        } catch (Exception e) {
            log.error("代理访问图片失败: {}", objectName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 图片压缩测试接口 - 上传并压缩图片
     *
     * @param file 图片文件
     * @param quality 压缩质量 (0.1-1.0，可选，默认智能压缩)
     * @param maxWidth 最大宽度 (可选，默认1920)
     * @param maxHeight 最大高度 (可选，默认1920)
     * @return 压缩结果对比
     */
    @PostMapping("/compress/test")
    public ApiResponse<Map<String, Object>> compressImageTest(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality", required = false) Double quality,
            @RequestParam(value = "maxWidth", required = false, defaultValue = "1920") Integer maxWidth,
            @RequestParam(value = "maxHeight", required = false, defaultValue = "1920") Integer maxHeight) {
        try {
            log.info("接收到图片压缩测试请求: fileName={}, size={}, contentType={}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
            
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            // 检查是否为图片文件
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("仅支持图片文件");
            }

            long originalSize = file.getSize();
            byte[] compressedBytes;

            // 根据是否指定质量参数选择压缩方式
            if (quality != null) {
                // 自定义压缩参数
                if (quality < 0.1 || quality > 1.0) {
                    return ApiResponse.error("压缩质量必须在0.1-1.0之间");
                }
                compressedBytes = imageCompressionService.compressImage(file, quality, maxWidth, maxHeight);
                log.info("使用自定义压缩参数: quality={}, maxSize={}x{}", quality, maxWidth, maxHeight);
            } else {
                // 智能压缩
                compressedBytes = imageCompressionService.smartCompress(file);
                log.info("使用智能压缩策略");
            }

            long compressedSize = compressedBytes.length;
            double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("originalFileName", file.getOriginalFilename());
            result.put("originalSize", originalSize);
            result.put("originalSizeReadable", formatFileSize(originalSize));
            result.put("compressedSize", compressedSize);
            result.put("compressedSizeReadable", formatFileSize(compressedSize));
            result.put("compressionRatio", String.format("%.1f%%", compressionRatio));
            result.put("needsCompression", imageCompressionService.needsCompression(originalSize));
            
            // 压缩参数信息
            Map<String, Object> compressionParams = new HashMap<>();
            compressionParams.put("quality", quality != null ? quality : "智能选择");
            compressionParams.put("maxWidth", maxWidth);
            compressionParams.put("maxHeight", maxHeight);
            compressionParams.put("outputFormat", "JPEG");
            result.put("compressionParams", compressionParams);

            String message = String.format("图片压缩测试完成，压缩率: %.1f%%", compressionRatio);
            return ApiResponse.success(message, result);

        } catch (Exception e) {
            log.error("图片压缩测试失败", e);
            return ApiResponse.error("图片压缩测试失败: " + e.getMessage());
        }
    }

    /**
     * 图片压缩并上传测试接口 - 压缩后上传到OSS
     *
     * @param file 图片文件
     * @param quality 压缩质量 (0.1-1.0，可选，默认智能压缩)
     * @param maxWidth 最大宽度 (可选，默认1920)
     * @param maxHeight 最大高度 (可选，默认1920)
     * @return 上传结果
     */
    @PostMapping("/compress/upload")
    public ApiResponse<Map<String, Object>> compressAndUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "quality", required = false) Double quality,
            @RequestParam(value = "maxWidth", required = false, defaultValue = "1920") Integer maxWidth,
            @RequestParam(value = "maxHeight", required = false, defaultValue = "1920") Integer maxHeight) {
        try {
            log.info("接收到图片压缩上传请求: fileName={}, size={}, contentType={}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
            
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            // 检查是否为图片文件
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ApiResponse.error("仅支持图片文件");
            }

            long originalSize = file.getSize();
            byte[] compressedBytes;

            // 根据是否指定质量参数选择压缩方式
            if (quality != null) {
                if (quality < 0.1 || quality > 1.0) {
                    return ApiResponse.error("压缩质量必须在0.1-1.0之间");
                }
                compressedBytes = imageCompressionService.compressImage(file, quality, maxWidth, maxHeight);
            } else {
                compressedBytes = imageCompressionService.smartCompress(file);
            }

            // TODO: 这里需要创建一个临时的MultipartFile来上传压缩后的图片
            // 暂时返回压缩信息，实际上传功能需要进一步实现
            
            long compressedSize = compressedBytes.length;
            double compressionRatio = (1 - (double) compressedSize / originalSize) * 100;

            Map<String, Object> result = new HashMap<>();
            result.put("originalFileName", file.getOriginalFilename());
            result.put("originalSize", originalSize);
            result.put("originalSizeReadable", formatFileSize(originalSize));
            result.put("compressedSize", compressedSize);
            result.put("compressedSizeReadable", formatFileSize(compressedSize));
            result.put("compressionRatio", String.format("%.1f%%", compressionRatio));
            result.put("message", "图片压缩完成，准备上传功能需要进一步实现");

            return ApiResponse.success("图片压缩完成", result);

        } catch (Exception e) {
            log.error("图片压缩上传失败", e);
            return ApiResponse.error("图片压缩上传失败: " + e.getMessage());
        }
    }

    /**
     * 格式化文件大小显示
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
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
            default:
                return "image/jpeg";
        }
    }
}
