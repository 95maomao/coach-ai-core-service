#!/bin/bash

# Docker构建脚本
# 用于本地测试Docker镜像构建

echo "开始构建Docker镜像..."

# 1. 清理并构建Maven项目
echo "步骤1: 构建Maven项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven构建失败！"
    exit 1
fi

# 2. 构建Docker镜像
echo "步骤2: 构建Docker镜像..."
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
