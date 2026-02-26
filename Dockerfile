# 使用 Java 17 的 Maven 基础镜像进行构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 pom.xml
COPY pom.xml .

# 复制源代码
COPY src ./src

# 编译并打包（跳过测试）
RUN mvn clean package -DskipTests -Dorg.slf4j.simpleLogger.defaultLogLevel=info

# 第二阶段：运行应用
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/target/mock-service-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

