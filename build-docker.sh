#!/bin/bash

# Docker构建脚本
# 用于本地测试Docker镜像构建

echo "开始构建Docker镜像..."

# 构建Docker镜像（多阶段构建，无需本地Maven构建）
echo "构建Docker镜像（多阶段构建）..."
docker build -t coach-ai-core-service:latest .

if [ $? -ne 0 ]; then
    echo "Docker构建失败！"
    exit 1
fi

echo "Docker镜像构建成功！"
echo "镜像名称: coach-ai-core-service:latest"
echo ""
echo "运行镜像命令:"
echo "docker run -p 8080:8080 coach-ai-core-service:latest"
echo ""
echo "测试API命令:"
echo "curl http://localhost:8080/api/hello"
echo "curl http://localhost:8080/api/health"
