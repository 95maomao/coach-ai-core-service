# 姿态分析API实现说明

## 概述
本文档描述了姿态分析功能的完整实现，包括AI工作流调用、数据库存储和接口设计。

## 实现的功能

### 1. 姿态分析接口
- **路径**: `POST /api/data/pose-analysis`
- **功能**: 接收用户图片链接和基本信息，调用AI工作流进行姿态分析，并将结果存储到数据库

### 2. 请求参数
```json
{
  "imageLink": "http://example.com/image.jpg",
  "username": "用户名",
  "sport": "YOGA",
  "posture": "Downward Dog"
}
```

### 3. 响应格式
```json
{
  "result": "SUCCESS",
  "message": "姿态分析完成",
  "data": {
    "id": 1,
    "username": "用户名",
    "sport": "YOGA",
    "posture": "Downward Dog",
    "userPoseImage": "用户姿势图片链接",
    "referencePoseImage": "参考姿势图片链接",
    "analysisResults": "[{\"problem\":\"问题描述\",\"suggestion\":\"建议\",\"isLastProblem\":false}]",
    "improvementResults": "[{\"problem\":\"问题描述\",\"evaluation\":\"评估结果\"}]",
    "createdAt": 1632150000000,
    "updatedAt": 1632150000000
  }
}
```

## 核心实现

### 1. AI工作流配置
在 `application.yml` 中配置了AI工作流的相关参数：
```yaml
ai:
  workflow:
    base-url: https://apaas.alibaba-inc.com/common/invoke
    timeout: 120000  # 2分钟超时
    pose-analysis:
      api-code: 360000000003870008
      ak: 445752-b3f1-mbs3Z1OiBL2u4fo5NDwe
```

### 2. 主要组件

#### AiWorkflowService
- 负责调用外部AI工作流API
- 解析三层嵌套的JSON响应
- 支持2分钟超时设置

#### PoseAnalysisRecordService
- 管理姿态分析记录的CRUD操作
- 提供获取用户历史问题的功能（用于lastProblem参数）

#### DataController
- 提供姿态分析的HTTP接口
- 整合AI工作流调用和数据库存储

### 3. 数据流程

1. **接收请求**: 前端发送包含imageLink、username、sport、posture的请求
2. **查询历史**: 从数据库查询用户该姿势的最新记录，提取analysisResults作为lastProblem
3. **调用AI工作流**: 构建请求参数，调用阿里云AI工作流API
4. **解析响应**: 解析三层嵌套的JSON响应，提取分析结果
5. **存储数据**: 将分析结果存储到PoseAnalysisRecord表
6. **返回结果**: 返回包含完整分析结果的响应

### 4. 错误处理
- AI工作流调用失败时返回错误信息
- JSON解析失败时返回错误信息
- 数据库操作失败时返回错误信息
- 所有错误都会记录详细日志

### 5. 性能优化
- 使用RestTemplate配置合适的超时时间
- 异常情况下返回空的lastProblem列表，不影响主流程
- 详细的日志记录便于问题排查

## 数据库设计

### PoseAnalysisRecord表结构
- `id`: 主键
- `username`: 用户名
- `sport`: 运动类型（枚举）
- `posture`: 姿势名称
- `user_pose_image`: 用户姿势图片链接
- `reference_pose_image`: 参考姿势图片链接
- `analysis_results`: 分析结果（JSON格式）
- `improvement_results`: 改进结果（JSON格式）
- `created_at`: 创建时间
- `updated_at`: 更新时间

## 使用示例

### 第一次分析（无历史记录）
```bash
curl -X POST "http://localhost:8080/api/data/pose-analysis" \
  -H "Content-Type: application/json" \
  -d '{
    "imageLink": "http://example.com/user-pose.jpg",
    "username": "张三",
    "sport": "YOGA",
    "posture": "Downward Dog"
  }'
```

### 后续分析（有历史记录）
系统会自动查询用户该姿势的最新记录，将之前的analysisResults作为lastProblem参数传递给AI工作流。

## 注意事项

1. **超时设置**: AI工作流调用可能需要30秒到2分钟，已设置合适的超时时间
2. **数据格式**: analysisResults和improvementResults以JSON字符串格式存储
3. **错误恢复**: 获取lastProblem失败时返回空列表，确保主流程不受影响
4. **日志记录**: 所有关键步骤都有详细的日志记录，便于问题排查
