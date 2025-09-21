# MinIO到OSS迁移指南

## 概述
本指南详细说明如何将CoachAI项目从MinIO文件存储迁移到阿里云OSS。

## 迁移前准备

### 1. OSS服务准备
- ✅ 已开通阿里云OSS服务
- ✅ 已创建Bucket: `cri-537ixr7zm3jj17zu-registry`
- ✅ 已创建RAM用户并获取AccessKey
- ✅ 已配置适当的权限策略

### 2. 配置信息
```yaml
OSS配置:
  endpoint: https://oss-cn-shenzhen.aliyuncs.com
  bucket-name: your-bucket-name
  region: cn-shenzhen
  access-key-id: YOUR_ACCESS_KEY_ID
  access-key-secret: YOUR_ACCESS_KEY_SECRET
```

## 代码变更说明

### 1. 依赖更新
- ❌ 移除: `io.minio:minio:8.5.7`
- ✅ 添加: `com.aliyun.oss:aliyun-sdk-oss:3.17.4`

### 2. 配置文件变更
- ❌ 删除: `MinioConfig.java`
- ✅ 新增: `OssConfig.java`
- ✅ 更新: `application.yml` (MinIO配置 → OSS配置)

### 3. 服务实现变更
- ❌ 移除: MinIO客户端相关代码
- ✅ 新增: `OssFileStorageService.java`
- ✅ 更新: `FileController.java` (使用OSS服务)

### 4. Kubernetes配置变更
- ✅ 更新: `coach-ai-core-service.yaml` (环境变量)
- ✅ 新增: `oss-secret.yaml` (OSS密钥配置)

## 部署步骤

### 步骤1: 准备OSS Secret
```bash
# 1. 生成AccessKey的base64编码
echo -n "你的实际AccessKey-ID" | base64
echo -n "你的实际AccessKey-Secret" | base64

# 2. 更新oss-secret.yaml中的值
# 3. 应用Secret配置
kubectl apply -f manifests/oss-secret.yaml
```

### 步骤2: 数据迁移（可选）
如果需要迁移现有MinIO数据到OSS：
```bash
# 1. 安装必要工具
# - MinIO Client (mc)
# - 阿里云OSS工具 (ossutil)

# 2. 配置迁移脚本中的AccessKey信息
vim scripts/migrate-minio-to-oss.sh

# 3. 执行迁移
./scripts/migrate-minio-to-oss.sh

# 4. 更新数据库中的URL（如果需要）
mysql -h 47.112.214.8 -u root -p coach_ai < /tmp/update_urls.sql
```

### 步骤3: 部署新版本
```bash
# 1. 构建新的Docker镜像
docker build -t your-registry/coach-ai-core-service:oss-v1.0 .

# 2. 推送镜像
docker push your-registry/coach-ai-core-service:oss-v1.0

# 3. 更新Kubernetes部署
kubectl set image deployment/coach-ai-core-service coach-ai-core-service=your-registry/coach-ai-core-service:oss-v1.0

# 4. 验证部署状态
kubectl get pods -l app=coach-ai-core-service
kubectl logs -f deployment/coach-ai-core-service
```

### 步骤4: 功能验证
```bash
# 1. 健康检查
curl http://your-service-ip/api/pose-analysis-records/health

# 2. 文件上传测试
curl -X POST -F "file=@test-image.jpg" http://your-service-ip/api/files/upload/image

# 3. 姿态分析测试
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"test","sport":"BASKETBALL","posture":"投篮","imageLink":"your-oss-image-url"}' \
  http://your-service-ip/api/pose-analysis-records/analyze
```

### 步骤5: 清理MinIO资源（确认无误后）
```bash
# 1. 删除MinIO相关资源
kubectl delete -f manifests/minio.yaml

# 2. 清理MinIO数据卷（谨慎操作）
# kubectl delete pvc minio-pvc
```

## 配置说明

### OSS配置参数
| 参数 | 说明 | 示例值 |
|------|------|--------|
| endpoint | OSS服务端点 | https://oss-cn-shenzhen.aliyuncs.com |
| access-key-id | RAM用户AccessKey ID | YOUR_ACCESS_KEY_ID |
| access-key-secret | RAM用户AccessKey Secret | YOUR_ACCESS_KEY_SECRET |
| bucket-name | OSS存储桶名称 | cri-537ixr7zm3jj17zu-registry |
| region | OSS地域代码 | cn-shenzhen |
| cdn-domain | CDN域名（可选） | https://cdn.example.com |
| custom-domain | 自定义域名（可选） | https://files.example.com |

### 环境变量
```bash
OSS_ENDPOINT=https://oss-cn-shenzhen.aliyuncs.com
OSS_ACCESS_KEY_ID=从Secret获取
OSS_ACCESS_KEY_SECRET=从Secret获取
OSS_BUCKET_NAME=cri-537ixr7zm3jj17zu-registry
OSS_REGION=cn-shenzhen
```

## 故障排查

### 常见问题

#### 1. AccessKey权限不足
**症状**: 上传文件时报403权限错误
**解决**: 检查RAM用户是否有OSS完整权限

#### 2. Bucket不存在
**症状**: 报NoSuchBucket错误
**解决**: 确认Bucket名称正确，或在OSS控制台创建Bucket

#### 3. 网络连接问题
**症状**: 连接超时或网络错误
**解决**: 检查网络连接，确认endpoint地址正确

#### 4. 文件URL访问问题
**症状**: 生成的URL无法访问
**解决**: 检查Bucket权限设置，确认是否需要预签名URL

### 日志查看
```bash
# 查看应用日志
kubectl logs -f deployment/coach-ai-core-service

# 查看特定Pod日志
kubectl logs -f pod/coach-ai-core-service-xxx

# 查看最近的事件
kubectl get events --sort-by=.metadata.creationTimestamp
```

## 性能优化建议

### 1. CDN加速
- 为OSS Bucket配置CDN加速
- 更新`cdn-domain`配置使用CDN域名

### 2. 图片压缩
- 利用OSS图片处理功能自动压缩
- 配置不同尺寸的图片规格

### 3. 缓存策略
- 设置合适的HTTP缓存头
- 利用浏览器缓存减少重复请求

### 4. 监控告警
- 配置OSS访问日志
- 设置流量和存储用量告警

## 回滚方案

如果迁移后出现问题，可以快速回滚：

### 1. 代码回滚
```bash
# 回滚到MinIO版本
kubectl rollout undo deployment/coach-ai-core-service

# 或指定特定版本
kubectl rollout undo deployment/coach-ai-core-service --to-revision=1
```

### 2. 重新启用MinIO
```bash
# 重新部署MinIO服务
kubectl apply -f manifests/minio.yaml

# 等待MinIO服务就绪
kubectl wait --for=condition=ready pod -l app=minio --timeout=300s
```

### 3. 数据恢复
如果有数据备份，可以从备份恢复MinIO数据。

## 联系支持

如果在迁移过程中遇到问题，请联系：
- 技术支持: [技术支持联系方式]
- 文档更新: [文档维护联系方式]

---

**注意**: 请在生产环境部署前，先在测试环境完整验证所有功能。
