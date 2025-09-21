#!/bin/bash

# MinIO到OSS数据迁移脚本
# 使用阿里云OSS命令行工具进行数据迁移

set -e

# 配置变量
MINIO_ENDPOINT="http://120.77.56.132:9000"
MINIO_ACCESS_KEY="minioadmin"
MINIO_SECRET_KEY="minioadmin123"
MINIO_BUCKET="coach-ai-files"

OSS_ENDPOINT="https://oss-cn-shenzhen.aliyuncs.com"
OSS_ACCESS_KEY_ID="你的OSS_ACCESS_KEY_ID"
OSS_ACCESS_KEY_SECRET="你的OSS_ACCESS_KEY_SECRET"
OSS_BUCKET="cri-537ixr7zm3jj17zu-registry"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== MinIO到OSS数据迁移脚本 ===${NC}"

# 检查依赖工具
check_dependencies() {
    echo -e "${YELLOW}检查依赖工具...${NC}"
    
    # 检查mc (MinIO Client)
    if ! command -v mc &> /dev/null; then
        echo -e "${RED}错误: MinIO Client (mc) 未安装${NC}"
        echo "请安装MinIO Client: https://docs.min.io/docs/minio-client-quickstart-guide.html"
        exit 1
    fi
    
    # 检查ossutil (阿里云OSS工具)
    if ! command -v ossutil &> /dev/null; then
        echo -e "${RED}错误: ossutil 未安装${NC}"
        echo "请安装ossutil: https://help.aliyun.com/document_detail/120075.html"
        exit 1
    fi
    
    echo -e "${GREEN}依赖工具检查完成${NC}"
}

# 配置MinIO客户端
configure_minio() {
    echo -e "${YELLOW}配置MinIO客户端...${NC}"
    mc alias set minio-source $MINIO_ENDPOINT $MINIO_ACCESS_KEY $MINIO_SECRET_KEY
    echo -e "${GREEN}MinIO客户端配置完成${NC}"
}

# 配置OSS客户端
configure_oss() {
    echo -e "${YELLOW}配置OSS客户端...${NC}"
    ossutil config -e $OSS_ENDPOINT -i $OSS_ACCESS_KEY_ID -k $OSS_ACCESS_KEY_SECRET
    echo -e "${GREEN}OSS客户端配置完成${NC}"
}

# 创建临时目录
create_temp_dir() {
    TEMP_DIR="/tmp/minio-to-oss-migration-$(date +%Y%m%d_%H%M%S)"
    mkdir -p $TEMP_DIR
    echo -e "${GREEN}创建临时目录: $TEMP_DIR${NC}"
}

# 从MinIO下载文件
download_from_minio() {
    echo -e "${YELLOW}从MinIO下载文件...${NC}"
    
    # 下载整个bucket
    mc mirror minio-source/$MINIO_BUCKET $TEMP_DIR/
    
    # 统计下载的文件数量
    FILE_COUNT=$(find $TEMP_DIR -type f | wc -l)
    echo -e "${GREEN}从MinIO下载完成，共 $FILE_COUNT 个文件${NC}"
}

# 上传到OSS
upload_to_oss() {
    echo -e "${YELLOW}上传文件到OSS...${NC}"
    
    # 检查OSS bucket是否存在，不存在则创建
    if ! ossutil ls oss://$OSS_BUCKET/ &> /dev/null; then
        echo -e "${YELLOW}创建OSS Bucket: $OSS_BUCKET${NC}"
        ossutil mb oss://$OSS_BUCKET/
    fi
    
    # 上传文件到OSS
    ossutil cp -r $TEMP_DIR/ oss://$OSS_BUCKET/ --update
    
    echo -e "${GREEN}上传到OSS完成${NC}"
}

# 验证迁移结果
verify_migration() {
    echo -e "${YELLOW}验证迁移结果...${NC}"
    
    # 统计MinIO中的文件数量
    MINIO_COUNT=$(mc ls --recursive minio-source/$MINIO_BUCKET/ | wc -l)
    
    # 统计OSS中的文件数量
    OSS_COUNT=$(ossutil ls oss://$OSS_BUCKET/ --recursive | grep -v "^Bucket" | grep -v "^Object Number" | wc -l)
    
    echo -e "${GREEN}MinIO文件数量: $MINIO_COUNT${NC}"
    echo -e "${GREEN}OSS文件数量: $OSS_COUNT${NC}"
    
    if [ "$MINIO_COUNT" -eq "$OSS_COUNT" ]; then
        echo -e "${GREEN}✓ 迁移验证成功，文件数量一致${NC}"
    else
        echo -e "${RED}✗ 迁移验证失败，文件数量不一致${NC}"
        exit 1
    fi
}

# 清理临时文件
cleanup() {
    echo -e "${YELLOW}清理临时文件...${NC}"
    rm -rf $TEMP_DIR
    echo -e "${GREEN}清理完成${NC}"
}

# 更新数据库中的URL
update_database_urls() {
    echo -e "${YELLOW}更新数据库中的文件URL...${NC}"
    
    # 这里需要根据实际情况编写SQL更新语句
    cat << EOF > /tmp/update_urls.sql
-- 更新姿态分析记录表中的图片URL
UPDATE pose_analysis_record 
SET user_pose_image = REPLACE(user_pose_image, 'http://120.77.56.132:9000/coach-ai-files/', 'https://cri-537ixr7zm3jj17zu-registry.oss-cn-shenzhen.aliyuncs.com/')
WHERE user_pose_image LIKE 'http://120.77.56.132:9000/coach-ai-files/%';

UPDATE pose_analysis_record 
SET reference_pose_image = REPLACE(reference_pose_image, 'http://120.77.56.132:9000/coach-ai-files/', 'https://cri-537ixr7zm3jj17zu-registry.oss-cn-shenzhen.aliyuncs.com/')
WHERE reference_pose_image LIKE 'http://120.77.56.132:9000/coach-ai-files/%';

-- 如果有其他表存储了文件URL，也需要类似更新
-- 请根据实际数据库结构添加相应的UPDATE语句
EOF
    
    echo -e "${GREEN}SQL更新脚本已生成: /tmp/update_urls.sql${NC}"
    echo -e "${YELLOW}请手动执行SQL脚本更新数据库中的URL${NC}"
}

# 主函数
main() {
    echo -e "${GREEN}开始MinIO到OSS迁移...${NC}"
    
    # 检查参数
    if [ "$OSS_ACCESS_KEY_ID" = "你的OSS_ACCESS_KEY_ID" ] || [ "$OSS_ACCESS_KEY_SECRET" = "你的OSS_ACCESS_KEY_SECRET" ]; then
        echo -e "${RED}错误: 请先在脚本中配置正确的OSS AccessKey信息${NC}"
        exit 1
    fi
    
    check_dependencies
    configure_minio
    configure_oss
    create_temp_dir
    
    # 执行迁移
    download_from_minio
    upload_to_oss
    verify_migration
    
    # 生成数据库更新脚本
    update_database_urls
    
    # 清理临时文件
    cleanup
    
    echo -e "${GREEN}=== 迁移完成 ===${NC}"
    echo -e "${YELLOW}注意事项:${NC}"
    echo -e "1. 请执行 /tmp/update_urls.sql 更新数据库中的URL"
    echo -e "2. 请测试应用功能确保一切正常"
    echo -e "3. 确认无误后可以停止MinIO服务"
}

# 执行主函数
main "$@"
