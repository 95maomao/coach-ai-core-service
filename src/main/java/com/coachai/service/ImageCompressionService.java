package com.coachai.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片压缩服务
 */
@Service
@Slf4j
public class ImageCompressionService {

    /**
     * 压缩图片文件
     *
     * @param file 原始图片文件
     * @param quality 压缩质量 (0.1-1.0)
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的图片字节数组
     */
    public byte[] compressImage(MultipartFile file, double quality, int maxWidth, int maxHeight) throws IOException {
        log.info("开始压缩图片: fileName={}, originalSize={} bytes, quality={}, maxSize={}x{}", 
                file.getOriginalFilename(), file.getSize(), quality, maxWidth, maxHeight);

        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .size(maxWidth, maxHeight)
                    .outputQuality(quality)
                    .outputFormat("jpg") // 统一输出为JPEG格式以获得更好的压缩效果
                    .toOutputStream(outputStream);

            byte[] compressedBytes = outputStream.toByteArray();
            
            log.info("图片压缩完成: originalSize={} bytes, compressedSize={} bytes, compression={}%", 
                    file.getSize(), compressedBytes.length, 
                    String.format("%.1f", (1 - (double) compressedBytes.length / file.getSize()) * 100));

            return compressedBytes;
        }
    }

    /**
     * 压缩Base64图片
     *
     * @param imageBytes 原始图片字节数组
     * @param quality 压缩质量 (0.1-1.0)
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的图片字节数组
     */
    public byte[] compressImageBytes(byte[] imageBytes, double quality, int maxWidth, int maxHeight) throws IOException {
        log.info("开始压缩图片字节数组: originalSize={} bytes, quality={}, maxSize={}x{}", 
                imageBytes.length, quality, maxWidth, maxHeight);

        try (InputStream inputStream = new ByteArrayInputStream(imageBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Thumbnails.of(inputStream)
                    .size(maxWidth, maxHeight)
                    .outputQuality(quality)
                    .outputFormat("jpg") // 统一输出为JPEG格式
                    .toOutputStream(outputStream);

            byte[] compressedBytes = outputStream.toByteArray();
            
            log.info("图片字节数组压缩完成: originalSize={} bytes, compressedSize={} bytes, compression={}%", 
                    imageBytes.length, compressedBytes.length, 
                    String.format("%.1f", (1 - (double) compressedBytes.length / imageBytes.length) * 100));

            return compressedBytes;
        }
    }

    /**
     * 智能压缩 - 根据图片大小自动选择压缩参数
     *
     * @param file 原始图片文件
     * @return 压缩后的图片字节数组
     */
    public byte[] smartCompress(MultipartFile file) throws IOException {
        long fileSize = file.getSize();
        double quality;
        int maxWidth, maxHeight;

        // 根据文件大小智能选择压缩参数
        if (fileSize > 5 * 1024 * 1024) { // 大于5MB
            quality = 0.6;
            maxWidth = 1920;
            maxHeight = 1920;
            log.info("大文件压缩策略: 质量=0.6, 最大尺寸=1920x1920");
        } else if (fileSize > 2 * 1024 * 1024) { // 大于2MB
            quality = 0.7;
            maxWidth = 1920;
            maxHeight = 1920;
            log.info("中等文件压缩策略: 质量=0.7, 最大尺寸=1920x1920");
        } else if (fileSize > 500 * 1024) { // 大于500KB
            quality = 0.8;
            maxWidth = 1920;
            maxHeight = 1920;
            log.info("小文件压缩策略: 质量=0.8, 最大尺寸=1920x1920");
        } else {
            // 小于500KB的图片不压缩，直接返回原文件
            log.info("文件过小，跳过压缩: {} bytes", fileSize);
            try {
                return file.getBytes();
            } catch (IOException e) {
                log.error("读取原文件失败", e);
                throw e;
            }
        }

        return compressImage(file, quality, maxWidth, maxHeight);
    }

    /**
     * 智能压缩字节数组
     *
     * @param imageBytes 原始图片字节数组
     * @return 压缩后的图片字节数组
     */
    public byte[] smartCompressBytes(byte[] imageBytes) throws IOException {
        long fileSize = imageBytes.length;
        double quality;
        int maxWidth, maxHeight;

        // 根据文件大小智能选择压缩参数
        if (fileSize > 5 * 1024 * 1024) { // 大于5MB
            quality = 0.6;
            maxWidth = 1920;
            maxHeight = 1920;
            log.info("大文件压缩策略: 质量=0.6, 最大尺寸=1920x1920");
        } else if (fileSize > 2 * 1024 * 1024) { // 大于2MB
            quality = 0.7;
            maxWidth = 1920;
            maxHeight = 1920;
            log.info("中等文件压缩策略: 质量=0.7, 最大尺寸=1920x1920");
        } else if (fileSize > 500 * 1024) { // 大于500KB
            quality = 0.8;
            maxWidth = 1920;
            maxHeight = 1920;
            log.info("小文件压缩策略: 质量=0.8, 最大尺寸=1920x1920");
        } else {
            // 小于500KB的图片不压缩
            log.info("文件过小，跳过压缩: {} bytes", fileSize);
            return imageBytes;
        }

        return compressImageBytes(imageBytes, quality, maxWidth, maxHeight);
    }

    /**
     * 检查是否需要压缩
     *
     * @param fileSize 文件大小（字节）
     * @return 是否需要压缩
     */
    public boolean needsCompression(long fileSize) {
        return fileSize > 500 * 1024; // 大于500KB需要压缩
    }
}
