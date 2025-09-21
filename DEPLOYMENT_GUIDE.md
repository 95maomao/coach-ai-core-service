# CoachAI 部署指南

## 概述
本指南说明如何安全地部署CoachAI应用，包括所有必要的环境变量和密钥配置。

## 部署前准备

### 1. 环境变量配置
在部署前，需要准备以下环境变量：

#### 数据库配置
- `MYSQL_HOST`: MySQL服务器地址
- `MYSQL_PORT`: MySQL端口（默认3306）
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_USERNAME`: 数据库用户名
- `MYSQL_PASSWORD`: 数据库密码

#### OSS配置
- `OSS_ENDPOINT`: OSS服务端点
- `OSS_ACCESS_KEY_ID`: OSS访问密钥ID
- `OSS_ACCESS_KEY_SECRET`: OSS访问密钥Secret
- `OSS_BUCKET_NAME`: OSS存储桶名称
- `OSS_REGION`: OSS地域代码
- `OSS_SECURE`: 是否使用HTTPS（默认true）
- `OSS_CONNECTION_TIMEOUT`: 连接超时时间（默认50000ms）
- `OSS_SOCKET_TIMEOUT`: Socket超时时间（默认50000ms）

### 2. Kubernetes部署

#### 步骤1: 创建OSS密钥
```bash
# 1. 生成AccessKey的base64编码
echo -n "你的实际AccessKey-ID" | base64
echo -n "你的实际AccessKey-Secret" | base64

# 2. 编辑oss-secret.yaml，替换占位符
vim manifests/oss-secret.yaml

# 3. 应用Secret配置
kubectl apply -f manifests/oss-secret.yaml
```

#### 步骤2: 部署应用
```bash
# 1. 构建Docker镜像
docker build -t your-registry/coach-ai-core-service:latest .

# 2. 推送镜像
docker push your-registry/coach-ai-core-service:latest

# 3. 更新deployment中的镜像地址
# 编辑 manifests/coach-ai-core-service.yaml，设置正确的IMAGE变量

# 4. 部署应用
kubectl apply -f manifests/coach-ai-core-service.yaml
```

#### 步骤3: 验证部署
```bash
# 检查Pod状态
kubectl get pods -l app=coach-ai-core-service

# 查看日志
kubectl logs -l app=coach-ai-core-service

# 检查Service
kubectl get svc coach-ai-core-service-lb
```

## 安全注意事项

### 1. 密钥管理
- ✅ 所有敏感信息都通过环境变量或Kubernetes Secret管理
- ✅ 代码中不包含硬编码的密钥信息
- ✅ 使用最小权限原则配置OSS访问权限

### 2. 网络安全
- 建议使用VPC内网访问数据库
- 配置适当的安全组规则
- 启用OSS的HTTPS访问

### 3. 监控和日志
- 配置应用日志收集
- 设置监控告警
- 定期检查访问日志

## 故障排查

### 常见问题
1. **OSS连接失败**
   - 检查AccessKey是否正确
   - 验证网络连接
   - 确认Bucket权限

2. **数据库连接失败**
   - 检查数据库连接信息
   - 验证网络连通性
   - 确认用户权限

3. **Pod启动失败**
   - 查看Pod日志：`kubectl logs <pod-name>`
   - 检查环境变量配置
   - 验证镜像是否正确

### 日志查看
```bash
# 查看应用日志
kubectl logs -f deployment/coach-ai-core-service

# 查看事件
kubectl get events --sort-by=.metadata.creationTimestamp
```

## 配置文件说明

### application.yml
- 包含应用的基础配置
- 所有敏感信息都通过环境变量引用
- 提供合理的默认值

### manifests/oss-secret.yaml
- 存储OSS访问密钥的Kubernetes Secret
- 需要手动替换占位符为实际的base64编码值

### manifests/coach-ai-core-service.yaml
- 主要的Kubernetes部署配置
- 包含完整的环境变量配置
- 引用oss-secret中的敏感信息
