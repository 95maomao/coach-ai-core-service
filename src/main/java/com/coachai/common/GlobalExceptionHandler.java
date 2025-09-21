package com.coachai.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.persistence.EntityNotFoundException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理实体未找到异常
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error("参数校验失败: " + errorMessage));
    }

    /**
     * 处理文件上传相关异常
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<?>> handleMultipartException(MultipartException ex) {
        log.error("文件上传异常", ex);
        String message = "文件上传失败";
        
        if (ex.getMessage().contains("no multipart boundary was found")) {
            message = "文件上传失败: 请求格式错误，请使用正确的multipart/form-data格式。" +
                     "正确的curl命令示例: curl -F 'file=@your-file.jpg' http://your-server/api/files/upload/image";
        } else if (ex.getMessage().contains("Maximum upload size exceeded")) {
            message = "文件上传失败: 文件大小超过限制";
        } else {
            message = "文件上传失败: " + ex.getMessage();
        }
        
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    /**
     * 处理缺少请求部分异常
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        log.error("缺少请求部分", ex);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("缺少必需的文件参数: " + ex.getRequestPartName()));
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系统内部错误: " + ex.getMessage()));
    }
}
