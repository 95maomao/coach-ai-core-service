# Coach AI Core Service

一个基于Spring Boot的微服务应用，提供RESTful API接口。

## 功能特性

- Spring Boot 2.7.18
- Java 11
- RESTful API接口
- 健康检查接口
- Docker支持

## API接口

- `GET /api/hello` - 欢迎接口
- `GET /api/health` - 健康检查接口

## 本地开发

### 启动应用
```bash
mvn spring-boot:run
```

### 构建JAR包
```bash
mvn clean package
```

## Docker部署

### 本地Docker构建
```bash
# 使用构建脚本
./build-docker.sh

# 或手动构建
mvn clean package
docker build -t coach-ai-core-service:latest .
```

### 运行Docker容器
```bash
docker run -p 8080:8080 coach-ai-core-service:latest
```

### 测试API
```bash
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/health
```

## 阿里云ACR部署

项目已配置支持阿里云容器镜像服务(ACR)的自动构建：

1. Dockerfile位于项目根目录
2. 构建上下文目录设置为 `/`
3. 支持main分支代码变更自动构建

## 项目结构

```
coach-ai-core-service/
├── src/
│   ├── main/java/com/coachai/
│   │   ├── Application.java          # Spring Boot主启动类
│   │   └── controller/
│   │       └── TestController.java   # REST控制器
│   └── main/resources/               # 资源文件
├── target/                           # Maven构建输出
├── Dockerfile                        # Docker镜像构建文件
├── .dockerignore                     # Docker忽略文件
├── build-docker.sh                   # Docker构建脚本
└── pom.xml                          # Maven配置文件
```