@echo off
setlocal enabledelayedexpansion

REM Mock Service Docker 启动脚本 (Windows)

set "PROJECT_DIR=%~dp0"
set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

cls
echo ════════════════════════════════════════════════════════════
echo   Mock Service Backend - Docker 启动脚本
echo ════════════════════════════════════════════════════════════
echo.

REM 检查 Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: Docker 未安装或不在 PATH 中
    echo 请先安装 Docker Desktop: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)

REM 检查 Docker Compose
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: Docker Compose 未安装
    echo 请先安装 Docker Desktop（包含 Compose）
    pause
    exit /b 1
)

REM 检查 Docker daemon
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: Docker daemon 未运行
    echo 请启动 Docker Desktop
    pause
    exit /b 1
)

echo ✓ Docker 已准备就绪
echo.

REM 检查是否需要构建
if not exist "%PROJECT_DIR%\target\mock-service-backend-1.0.0.jar" (
    echo ⚠️  JAR 文件不存在，需要构建...
    echo.
    echo 为避免本地 Java 版本兼容性问题，将使用 Docker 进行构建。
    echo 请确保网络连接正常...
    echo.

    set /p "response=是否继续？(y/n): "
    if /i not "!response!"=="y" (
        echo 已取消构建。可以稍后手动运行：build-docker.bat
        pause
        exit /b 1
    )

    call "%PROJECT_DIR%\build-docker.bat"
    if errorlevel 1 (
        echo 构建失败！
        pause
        exit /b 1
    )
)

echo.
echo ════════════════════════════════════════════════════════════
echo   启动 Docker 容器...
echo ════════════════════════════════════════════════════════════
echo.

cd /d "%PROJECT_DIR%"

REM 检查 docker-compose 文件
if not exist "docker-compose.yml" (
    echo ❌ 错误: docker-compose.yml 未找到
    pause
    exit /b 1
)

REM 启动容器
docker-compose up -d

if errorlevel 1 (
    echo ❌ 容器启动失败！
    echo 请检查错误信息或运行：docker-compose logs
    pause
    exit /b 1
)

echo.
echo ✓ 容器已启动
echo.

REM 等待应用启动
echo ⏳ 等待应用启动（约 15-30 秒）...
timeout /t 5 /nobreak >nul

setlocal enabledelayedexpansion
for /l %%i in (1,1,30) do (
    curl -s http://localhost:8080/api/health >nul 2>&1
    if errorlevel 0 (
        echo.
        echo ✓ 应用已就绪！
        goto :success
    )
    echo -n "."
    timeout /t 1 /nobreak >nul
)

:success
cls
echo ════════════════════════════════════════════════════════════
echo   启动完成！
echo ════════════════════════════════════════════════════════════
echo.
echo 📊 服务信息：
echo   • API 地址: http://localhost:8080/api
echo   • H2 控制台: http://localhost:8080/api/h2-console
echo.
echo 🔧 常用命令：
echo   • 查看日志: docker-compose logs -f mock-service
echo   • 停止服务: docker-compose down
echo   • 重启服务: docker-compose restart
echo.
echo 📖 更多信息: 查看 DOCKER_DEPLOYMENT.md
echo.
pause
