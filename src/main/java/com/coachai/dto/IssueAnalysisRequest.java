package com.coachai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 症状分析请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAnalysisRequest {

    private String username;

    private List<String> bodyParts;

    private String sport;

    private List<String> posture;

    private String description;

}
