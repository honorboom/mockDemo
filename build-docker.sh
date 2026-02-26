#!/bin/bash

# 使用 Docker 进行构建，避免本地 Java 25 与 Lombok 的兼容性问题

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "正在使用 Docker 和 Java 17 进行构建..."
echo "项目目录: $PROJECT_DIR"

# 创建临时 Dockerfile 用于构建
cat > "$PROJECT_DIR/Dockerfile.build" << 'EOF'
FROM maven:3.9-eclipse-temurin-17

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -q

CMD ["echo", "Build completed!"]
EOF

# 构建镜像
docker build -f "$PROJECT_DIR/Dockerfile.build" -t mock-service-builder "$PROJECT_DIR"

# 创建容器并复制 JAR 文件
CONTAINER=$(docker create mock-service-builder)
docker cp "$CONTAINER:/build/target/mock-service-backend-1.0.0.jar" "$PROJECT_DIR/target/mock-service-backend-1.0.0.jar"
docker rm "$CONTAINER"

# 清理临时 Dockerfile
rm -f "$PROJECT_DIR/Dockerfile.build"

echo "✓ JAR 文件已生成: $PROJECT_DIR/target/mock-service-backend-1.0.0.jar"
echo ""
echo "现在可以运行 docker-compose up -d 来启动服务"
