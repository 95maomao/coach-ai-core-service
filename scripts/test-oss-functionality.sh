#!/bin/bash

# OSS功能测试脚本
# 用于验证OSS替换MinIO后的功能是否正常

set -e

# 配置变量
SERVICE_URL="http://localhost:8080/api"  # 根据实际部署地址修改
TEST_IMAGE="test-image.jpg"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== OSS功能测试脚本 ===${NC}"

# 创建测试图片
create_test_image() {
    echo -e "${YELLOW}创建测试图片...${NC}"
    
    # 创建一个简单的测试图片（1x1像素的PNG）
    echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU77yQAAAABJRU5ErkJggg==" | base64 -d > $TEST_IMAGE
    
    echo -e "${GREEN}测试图片创建完成: $TEST_IMAGE${NC}"
}

# 测试健康检查
test_health_check() {
    echo -e "${YELLOW}测试健康检查...${NC}"
    
    response=$(curl -s -w "%{http_code}" -o /tmp/health_response.json "$SERVICE_URL/pose-analysis-records/health" || echo "000")
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ 健康检查通过${NC}"
        cat /tmp/health_response.json | jq '.' 2>/dev/null || cat /tmp/health_response.json
    else
        echo -e "${RED}✗ 健康检查失败，HTTP状态码: $response${NC}"
        return 1
    fi
}

# 测试图片上传
test_image_upload() {
    echo -e "${YELLOW}测试图片上传...${NC}"
    
    response=$(curl -s -w "%{http_code}" -o /tmp/upload_response.json \
        -X POST \
        -F "file=@$TEST_IMAGE" \
        "$SERVICE_URL/files/upload/image" || echo "000")
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ 图片上传成功${NC}"
        
        # 提取文件URL
        file_url=$(cat /tmp/upload_response.json | jq -r '.data.fileUrl' 2>/dev/null || echo "")
        
        if [ -n "$file_url" ] && [ "$file_url" != "null" ]; then
            echo -e "${GREEN}文件URL: $file_url${NC}"
            
            # 验证URL是否包含OSS域名
            if [[ $file_url == *"oss-cn-shenzhen.aliyuncs.com"* ]] || [[ $file_url == *"cri-537ixr7zm3jj17zu-registry"* ]]; then
                echo -e "${GREEN}✓ URL格式正确，包含OSS域名${NC}"
            else
                echo -e "${YELLOW}⚠ URL格式可能不正确: $file_url${NC}"
            fi
            
            # 保存URL供后续测试使用
            echo "$file_url" > /tmp/test_image_url.txt
        else
            echo -e "${RED}✗ 无法提取文件URL${NC}"
        fi
        
        cat /tmp/upload_response.json | jq '.' 2>/dev/null || cat /tmp/upload_response.json
    else
        echo -e "${RED}✗ 图片上传失败，HTTP状态码: $response${NC}"
        cat /tmp/upload_response.json 2>/dev/null || echo "无响应内容"
        return 1
    fi
}

# 测试Base64图片上传
test_base64_upload() {
    echo -e "${YELLOW}测试Base64图片上传...${NC}"
    
    # 创建Base64测试数据
    base64_data="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU77yQAAAABJRU5ErkJggg=="
    
    response=$(curl -s -w "%{http_code}" -o /tmp/base64_response.json \
        -X POST \
        -H "Content-Type: application/json" \
        -d "{\"base64Image\":\"$base64_data\",\"fileName\":\"test-base64.png\",\"description\":\"测试Base64上传\"}" \
        "$SERVICE_URL/files/upload/base64-json" || echo "000")
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ Base64图片上传成功${NC}"
        cat /tmp/base64_response.json | jq '.' 2>/dev/null || cat /tmp/base64_response.json
    else
        echo -e "${RED}✗ Base64图片上传失败，HTTP状态码: $response${NC}"
        cat /tmp/base64_response.json 2>/dev/null || echo "无响应内容"
        return 1
    fi
}

# 测试文件信息获取
test_file_info() {
    echo -e "${YELLOW}测试文件信息获取...${NC}"
    
    if [ ! -f /tmp/test_image_url.txt ]; then
        echo -e "${YELLOW}跳过文件信息测试（没有可用的文件URL）${NC}"
        return 0
    fi
    
    file_url=$(cat /tmp/test_image_url.txt)
    # 从URL中提取objectName
    object_name=$(echo "$file_url" | sed 's|.*cri-537ixr7zm3jj17zu-registry.oss-cn-shenzhen.aliyuncs.com/||' | sed 's|.*coach-ai-files/||')
    
    if [ -n "$object_name" ]; then
        response=$(curl -s -w "%{http_code}" -o /tmp/info_response.json \
            "$SERVICE_URL/files/info?objectName=$object_name" || echo "000")
        
        if [ "$response" = "200" ]; then
            echo -e "${GREEN}✓ 文件信息获取成功${NC}"
            cat /tmp/info_response.json | jq '.' 2>/dev/null || cat /tmp/info_response.json
        else
            echo -e "${RED}✗ 文件信息获取失败，HTTP状态码: $response${NC}"
            cat /tmp/info_response.json 2>/dev/null || echo "无响应内容"
        fi
    else
        echo -e "${YELLOW}跳过文件信息测试（无法提取objectName）${NC}"
    fi
}

# 测试姿态分析（如果有可用的图片URL）
test_pose_analysis() {
    echo -e "${YELLOW}测试姿态分析...${NC}"
    
    if [ ! -f /tmp/test_image_url.txt ]; then
        echo -e "${YELLOW}跳过姿态分析测试（没有可用的图片URL）${NC}"
        return 0
    fi
    
    image_url=$(cat /tmp/test_image_url.txt)
    
    response=$(curl -s -w "%{http_code}" -o /tmp/pose_response.json \
        -X POST \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"test-user\",\"sport\":\"BASKETBALL\",\"posture\":\"投篮\",\"imageLink\":\"$image_url\"}" \
        "$SERVICE_URL/pose-analysis-records/analyze" || echo "000")
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ 姿态分析请求成功${NC}"
        cat /tmp/pose_response.json | jq '.' 2>/dev/null || cat /tmp/pose_response.json
    else
        echo -e "${YELLOW}⚠ 姿态分析可能失败（这可能是正常的，因为AI服务可能不可用），HTTP状态码: $response${NC}"
        cat /tmp/pose_response.json 2>/dev/null || echo "无响应内容"
    fi
}

# 清理测试文件
cleanup() {
    echo -e "${YELLOW}清理测试文件...${NC}"
    rm -f $TEST_IMAGE
    rm -f /tmp/health_response.json
    rm -f /tmp/upload_response.json
    rm -f /tmp/base64_response.json
    rm -f /tmp/info_response.json
    rm -f /tmp/pose_response.json
    rm -f /tmp/test_image_url.txt
    echo -e "${GREEN}清理完成${NC}"
}

# 主函数
main() {
    echo -e "${GREEN}开始OSS功能测试...${NC}"
    echo -e "${YELLOW}服务地址: $SERVICE_URL${NC}"
    
    # 检查jq工具（用于JSON解析，可选）
    if ! command -v jq &> /dev/null; then
        echo -e "${YELLOW}注意: jq工具未安装，JSON输出将不会格式化${NC}"
    fi
    
    # 创建测试图片
    create_test_image
    
    # 执行测试
    test_health_check
    test_image_upload
    test_base64_upload
    test_file_info
    test_pose_analysis
    
    # 清理
    cleanup
    
    echo -e "${GREEN}=== OSS功能测试完成 ===${NC}"
    echo -e "${YELLOW}注意事项:${NC}"
    echo -e "1. 如果某些测试失败，请检查服务是否正常运行"
    echo -e "2. 请确认OSS配置和权限设置正确"
    echo -e "3. 姿态分析测试可能因AI服务不可用而失败，这是正常的"
}

# 执行主函数
main "$@"
