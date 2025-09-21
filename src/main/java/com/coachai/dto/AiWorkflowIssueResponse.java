package com.coachai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI工作流响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWorkflowIssueResponse {

    /**
     * 响应代码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 响应数据
     */
    private ResponseData data;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 响应数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponseData {
        /**
         * 使用量
         */
        private Integer usage;

        /**
         * 使用量映射
         */
        private Object usageMap;

        /**
         * 结果（JSON字符串）
         */
        private String result;
    }

    /**
     * 解析后的结果数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParsedResult {
        /**
         * 数据
         */
        private ParsedData data;

        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 请求ID
         */
        private String requestId;
    }

    /**
     * 解析后的数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParsedData {
        /**
         * 消息列表
         */
        private List<Object> message;

        /**
         * 结构化数据
         */
        private StructData structData;
    }

    /**
     * 结构化数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StructData {
        // 标准运动姿势参考（数组形式）
        private List<String> poseReference;

        // 症状描述
        private DiagnosisData message;

        // 康复视频（数组形式）
        private List<String> rehabilitationVideos;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiagnosisData {
        private String sport;
        private List<String> posture;
        private String riskLevel;
        private String primaryDiagnosis;
        private int confidence;
        private boolean isNormal;
        private List<Symptom> symptoms;
        private Treatment treatment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Symptom {
        private String name;
        private String severity;
        private String cause;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Treatment {
        private List<String> prevention;
        private List<String> immediate;
        private List<String> recovery;
        private List<String> followUp;
    }

}
