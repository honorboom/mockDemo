@echo off

REM 使用 Docker 进行构建，避免本地 Java 25 与 Lombok 的兼容性问题

setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

echo 正在使用 Docker 和 Java 17 进行构建...
echo 项目目录: %PROJECT_DIR%

REM 创建临时 Dockerfile 用于构建
(
echo FROM maven:3.9-eclipse-temurin-17
echo.
echo WORKDIR /build
echo.
echo COPY pom.xml .
echo COPY src ./src
echo.
echo RUN mvn clean package -DskipTests
) > "%PROJECT_DIR%\Dockerfile.build"

REM 构建镜像
docker build -f "%PROJECT_DIR%\Dockerfile.build" -t mock-service-builder "%PROJECT_DIR%"

if %ERRORLEVEL% NEQ 0 (
    echo 构建失败！
    del "%PROJECT_DIR%\Dockerfile.build"
    exit /b 1
)

REM 创建容器并复制 JAR 文件
for /f %%i in ('docker create mock-service-builder') do set CONTAINER=%%i

if not exist "%PROJECT_DIR%\target" mkdir "%PROJECT_DIR%\target"

docker cp "%CONTAINER%:/build/target/mock-service-backend-1.0.0.jar" "%PROJECT_DIR%\target\"
docker rm "%CONTAINER%"

REM 清理临时 Dockerfile
del "%PROJECT_DIR%\Dockerfile.build"

echo.
echo ✓ JAR 文件已生成: %PROJECT_DIR%\target\mock-service-backend-1.0.0.jar
echo.
echo 现在可以运行 docker-compose up -d 来启动服务
