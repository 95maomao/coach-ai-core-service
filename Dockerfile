# 第一阶段：编译打包
FROM mz-acr-registry.cn-shenzhen.cr.aliyuncs.com/coach_ai/maven:3.8.6-openjdk-11-slim AS build
WORKDIR /build

# 创建 Maven 配置目录并设置权限
RUN mkdir -p /root/.m2 && chmod 700 /root/.m2

# 先只拷贝pom.xml并下载依赖
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests

# 复制源码
COPY src /build/src

# 编译打包
RUN mvn -B clean package -DskipTests

# 第二阶段：生成最终运行镜像
FROM mz-acr-registry.cn-shenzhen.cr.aliyuncs.com/coach_ai/openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装curl用于健康检查
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 创建应用用户（安全考虑）
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 复制构建产物
COPY --from=build /build/target/coach-ai-core-service-*.jar app.jar

# 修改文件所有者
RUN chown -R appuser:appuser /app

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
