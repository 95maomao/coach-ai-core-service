#!/bin/bash

# 文件上传测试脚本

# 配置
SERVICE_URL="http://localhost:8080/api"  # 根据实际地址修改
TEST_IMAGE="test-image.jpg"

echo "=== 文件上传测试脚本 ==="

# 创建测试图片（1x1像素的JPEG）
create_test_image() {
    echo "创建测试图片..."
    # 创建一个简单的测试图片
    echo "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/2wBDAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQH/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwA/8A8A" | base64 -d > $TEST_IMAGE
    echo "测试图片创建完成: $TEST_IMAGE"
}

# 测试图片上传 - 正确的方式
test_image_upload_correct() {
    echo ""
    echo "=== 测试图片上传（正确方式）==="
    
    # 方式1：使用-F参数（推荐）
    echo "方式1：使用 curl -F 参数"
    curl -X POST \
         -F "file=@$TEST_IMAGE" \
         "$SERVICE_URL/files/upload/image" \
         -H "Accept: application/json" \
         -v
    
    echo ""
    echo "---"
    
    # 方式2：明确指定Content-Type（curl会自动处理boundary）
    echo "方式2：让curl自动处理Content-Type"
    curl -X POST \
         --form "file=@$TEST_IMAGE" \
         "$SERVICE_URL/files/upload/image"
    
    echo ""
}

# 错误的测试方式（会导致boundary错误）
test_image_upload_wrong() {
    echo ""
    echo "=== 错误的测试方式（会报boundary错误）==="
    
    # 错误方式1：手动设置Content-Type但没有boundary
    echo "错误方式1：手动设置Content-Type但没有boundary"
    curl -X POST \
         -H "Content-Type: multipart/form-data" \
         -F "file=@$TEST_IMAGE" \
         "$SERVICE_URL/files/upload/image"
    
    echo ""
    echo "---"
    
    # 错误方式2：使用错误的Content-Type
    echo "错误方式2：使用application/json但发送文件"
    curl -X POST \
         -H "Content-Type: application/json" \
         -F "file=@$TEST_IMAGE" \
         "$SERVICE_URL/files/upload/image"
    
    echo ""
}

# Base64上传测试
test_base64_upload() {
    echo ""
    echo "=== 测试Base64图片上传 ==="
    
    # Base64数据（1x1像素的PNG）
    BASE64_DATA="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU77yQAAAABJRU5ErkJggg=="
    
    # 方式1：通过URL参数
    echo "方式1：通过URL参数上传Base64"
    curl -X POST \
         -d "base64Image=$BASE64_DATA" \
         "$SERVICE_URL/files/upload/base64" \
         -H "Content-Type: application/x-www-form-urlencoded"
    
    echo ""
    echo "---"
    
    # 方式2：通过JSON请求体
    echo "方式2：通过JSON请求体上传Base64"
    curl -X POST \
         -H "Content-Type: application/json" \
         -d "{\"base64Image\":\"$BASE64_DATA\",\"fileName\":\"test-base64.png\",\"description\":\"测试Base64上传\"}" \
         "$SERVICE_URL/files/upload/base64-json"
    
    echo ""
}

# 健康检查
test_health() {
    echo ""
    echo "=== 健康检查 ==="
    curl -X GET "$SERVICE_URL/pose-analysis-records/health"
    echo ""
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
    echo ""
    
    # 创建测试文件
    create_test_image
    
    # 健康检查
    test_health
    
    # 正确的上传测试
    test_image_upload_correct
    
    # Base64上传测试
    test_base64_upload
    
    # 显示错误示例（注释掉，避免实际执行）
    # test_image_upload_wrong
    
    # 清理
    cleanup
    
    echo ""
    echo "=== 测试完成 ==="
    echo ""
    echo "注意事项："
    echo "1. 使用 curl -F 'file=@filename' 上传文件"
    echo "2. 不要手动设置 Content-Type: multipart/form-data"
    echo "3. 让curl自动处理multipart boundary"
    echo "4. 确保服务正在运行在指定地址"
}

# 执行
main "$@"
