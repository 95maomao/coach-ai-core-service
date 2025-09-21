#!/bin/bash

# Content-Disposition调试脚本

SERVICE_URL="http://localhost:8080/api"
TEST_IMAGE="debug-test.jpg"

echo "=== Content-Disposition 调试脚本 ==="

# 创建测试图片
create_test_image() {
    echo "创建测试图片..."
    # 创建一个简单的JPEG图片（红色1x1像素）
    echo "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A" | base64 -d > $TEST_IMAGE
    echo "测试图片创建完成: $TEST_IMAGE"
}

# 上传图片并获取URL
upload_and_test() {
    echo ""
    echo "=== 上传图片并测试 ==="
    
    # 上传图片
    echo "上传图片..."
    response=$(curl -s -X POST -F "file=@$TEST_IMAGE" "$SERVICE_URL/files/upload/image")
    echo "上传响应: $response"
    
    # 提取URL
    file_url=$(echo "$response" | grep -o '"fileUrl":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$file_url" ]; then
        echo "文件URL: $file_url"
        echo ""
        
        # 测试HTTP头
        echo "=== 检查HTTP响应头 ==="
        curl -I "$file_url"
        
        echo ""
        echo "=== 查找Content-Disposition头 ==="
        content_disposition=$(curl -s -I "$file_url" | grep -i "content-disposition" || echo "未找到Content-Disposition头")
        echo "Content-Disposition: $content_disposition"
        
        echo ""
        echo "=== 查找Content-Type头 ==="
        content_type=$(curl -s -I "$file_url" | grep -i "content-type" || echo "未找到Content-Type头")
        echo "Content-Type: $content_type"
        
        echo ""
        echo "=== 浏览器测试建议 ==="
        echo "请在浏览器中打开以下URL进行测试:"
        echo "$file_url"
        echo ""
        echo "预期行为: 图片应该在浏览器中直接显示，而不是下载"
        
    else
        echo "错误: 无法从响应中提取文件URL"
        echo "完整响应: $response"
    fi
}

# 清理
cleanup() {
    echo ""
    echo "清理测试文件..."
    rm -f $TEST_IMAGE
    echo "清理完成"
}

# 主函数
main() {
    echo "服务地址: $SERVICE_URL"
    
    # 检查服务是否运行
    echo "检查服务状态..."
    if curl -s "$SERVICE_URL/pose-analysis-records/health" > /dev/null; then
        echo "✓ 服务正在运行"
    else
        echo "✗ 服务未运行，请先启动服务"
        exit 1
    fi
    
    create_test_image
    upload_and_test
    cleanup
    
    echo ""
    echo "=== 调试完成 ==="
    echo ""
    echo "如果图片仍然触发下载，可能的原因："
    echo "1. OSS Bucket的默认设置覆盖了Content-Disposition"
    echo "2. 浏览器缓存了之前的响应头"
    echo "3. OSS的某些配置需要在控制台中设置"
    echo ""
    echo "建议："
    echo "1. 清除浏览器缓存或使用无痕模式"
    echo "2. 检查OSS控制台的Bucket设置"
    echo "3. 查看应用日志中的元数据信息"
}

# 执行
main "$@"
