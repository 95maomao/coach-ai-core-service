package com.coachai.controller;

import com.coachai.common.ApiResponse;
import com.coachai.dto.DataProcessRequest;
import com.coachai.dto.DataProcessResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;

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
}
