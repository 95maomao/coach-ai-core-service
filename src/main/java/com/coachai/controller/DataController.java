package com.coachai.controller;

import com.alibaba.fastjson.JSONObject;
import com.coachai.common.ApiResponse;
import com.coachai.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * 数据处理Controller
 */
@RestController
@RequestMapping("/data")
@Slf4j
public class DataController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 数据处理方法
     * 
     * @param request 包含JSON字符串和Base64字符串的请求对象
     * @return 处理后的固定格式JSON响应
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<DataProcessResponse>> processData(
            @RequestBody @Valid DataProcessRequest request) {
        
        log.info("接收到数据处理请求");
        
        try {
            // 1. 提取JSON中的nextNodeImageInstructions字段
            String nextNodeImageInstructions = extractNextNodeImageInstructions(request.getJsonString());
            
            // 2. 获取base64字符串内容
            String base64Content = request.getBase64String();
            
            // 3. 构造固定格式的响应
            DataProcessResponse.Part part = new DataProcessResponse.Part(base64Content, nextNodeImageInstructions);
            DataProcessResponse response = new DataProcessResponse("USER", Collections.singletonList(part));
            
            log.info("数据处理完成");
            return ResponseEntity.ok(ApiResponse.success("数据处理成功", response));
            
        } catch (Exception e) {
            log.error("数据处理失败", e);
            return ResponseEntity.ok(ApiResponse.error("数据处理失败: " + e.getMessage()));
        }
    }


    /**
     * 姿态分析处理方法
     *
     * @param request 包含JSON字符串、Base64字符串和类型的请求对象
     * @return 根据类型返回不同格式的JSON响应
     */
    @PostMapping("/pose-analysis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processPoseAnalysis(
            @RequestBody @Valid PoseAnalysisDataProcessRequest request) {

        log.info("接收到姿态分析处理请求，类型: {}", request.getType());

        try {
            // 验证type参数
            if (!"userPoseImage".equals(request.getType()) && !"referencePoseImage".equals(request.getType())) {
                throw new IllegalArgumentException("type参数必须为 userPoseImage 或 referencePoseImage");
            }

            // 根据type提取对应的指令字段
            String instructions = extractInstructionsByType(request.getJsonString(), request.getType());

            // 获取图片url
            String imageUrl = request.getImageUrl();

            // 构造响应
            Map<String, Object>  poseImageInstructions = generatePoseImageInstructions(instructions, imageUrl);

            log.info("姿态分析处理完成，类型: {}", request.getType());
            return ResponseEntity.ok(ApiResponse.success("姿态分析处理成功", poseImageInstructions));

        } catch (Exception e) {
            log.error("姿态分析处理失败", e);
            return ResponseEntity.ok(ApiResponse.error("姿态分析处理失败: " + e.getMessage()));
        }
    }

    private Map<String, Object>  generatePoseImageInstructions(String instructions, String imageUrl) {
        // 构建期望的数据结构
        Map<String, Object> response = new HashMap<>();
        response.put("role", "user");

        List<Map<String, Object>> parts = new ArrayList<>();

        // 添加文本部分
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", instructions);
        parts.add(textPart);

        // 添加文件数据部分
        Map<String, Object> filePart = new HashMap<>();
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("mimeType", "image/jpeg");
        fileData.put("fileUri", imageUrl);
        filePart.put("fileData", fileData);
        parts.add(filePart);

        response.put("parts", parts);

        return response;

    }


    /**
     * 从JSON字符串中提取nextNodeImageInstructions字段
     * 
     * @param jsonString JSON字符串
     * @return nextNodeImageInstructions字段的值
     * @throws Exception 解析异常
     */
    private String extractNextNodeImageInstructions(String jsonString) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            JsonNode instructionsNode = jsonNode.get("nextNodeImageInstructions");
            
            if (instructionsNode == null) {
                throw new IllegalArgumentException("JSON中未找到nextNodeImageInstructions字段");
            }
            
            return instructionsNode.asText();
        } catch (Exception e) {
            log.error("解析JSON字符串失败: {}", jsonString, e);
            throw new Exception("解析JSON字符串失败: " + e.getMessage());
        }
    }


    /**
     * 根据类型从JSON字符串中提取对应的指令字段
     *
     * @param jsonString JSON字符串
     * @param type 类型（userPoseImage 或 referencePoseImage）
     * @return 对应的指令字段值
     * @throws Exception 解析异常
     */
    private String extractInstructionsByType(String jsonString, String type) throws Exception {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            // 首先检查是否有answer字段
            JsonNode answerNode = jsonNode.get("answer");
            JsonNode targetNode;

            if (answerNode != null) {
                // 如果有answer字段，解析其中的JSON字符串
                String answerJsonString = answerNode.asText();
                targetNode = objectMapper.readTree(answerJsonString);
            } else {
                // 如果没有answer字段，直接在顶层查找
                targetNode = jsonNode;
            }

            JsonNode instructionsNode;

            if ("userPoseImage".equals(type)) {
                instructionsNode = targetNode.get("userPoseImageInstructions");
                if (instructionsNode == null) {
                    throw new IllegalArgumentException("JSON中未找到userPoseImageInstructions字段");
                }
            } else if ("referencePoseImage".equals(type)) {
                instructionsNode = targetNode.get("referencePoseImageInstructions");
                if (instructionsNode == null) {
                    throw new IllegalArgumentException("JSON中未找到referencePoseImageInstructions字段");
                }
            } else {
                throw new IllegalArgumentException("不支持的类型: " + type);
            }

            return instructionsNode.asText();
        } catch (Exception e) {
            log.error("解析JSON字符串失败，类型: {}, JSON: {}", type, jsonString, e);
            throw new Exception("解析JSON字符串失败: " + e.getMessage());
        }
    }
}
