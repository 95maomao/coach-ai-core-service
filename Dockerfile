# 第一阶段：编译打包
FROM mz-acr-registry.cn-shenzhen.cr.aliyuncs.com/coach_ai/maven:3.8.6-openjdk-11-slim AS build
WORKDIR /build

# 创建 Maven 配置目录并设置权限（避免使用/root）
RUN mkdir -p /build/.m2 && chmod 700 /build/.m2

# 配置阿里云Maven镜像加速
RUN echo '<?xml version="1.0" encoding="UTF-8"?>' > /build/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /build/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /build/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /build/.m2/settings.xml && \
    echo '  <mirrors>' >> /build/.m2/settings.xml && \
    echo '    <mirror>' >> /build/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /build/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /build/.m2/settings.xml && \
    echo '      <name>Aliyun Maven</name>' >> /build/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /build/.m2/settings.xml && \
    echo '    </mirror>' >> /build/.m2/settings.xml && \
    echo '  </mirrors>' >> /build/.m2/settings.xml && \
    echo '</settings>' >> /build/.m2/settings.xml

# 先只拷贝pom.xml并下载依赖
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests -s /build/.m2/settings.xml

# 复制源码
COPY src /build/src

# 编译打包
RUN mvn -B clean package -DskipTests -s /build/.m2/settings.xml

# 第二阶段：生成最终运行镜像
FROM mz-acr-registry.cn-shenzhen.cr.aliyuncs.com/coach_ai/openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制构建产物
COPY --from=build /build/target/coach-ai-core-service-*.jar app.jar

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
