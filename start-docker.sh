#!/bin/bash

# Mock Service Docker 启动脚本

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "════════════════════════════════════════════════════════════"
echo "  Mock Service Backend - Docker 启动脚本"
echo "════════════════════════════════════════════════════════════"
echo ""

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker 未安装或不在 PATH 中"
    echo "请先安装 Docker Desktop: https://www.docker.com/products/docker-desktop"
    exit 1
fi

# 检查 Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo "❌ 错误: Docker Compose 未安装"
    echo "请先安装 Docker Desktop（包含 Compose）"
    exit 1
fi

# 检查 Docker daemon
if ! docker info > /dev/null 2>&1; then
    echo "❌ 错误: Docker daemon 未运行"
    echo "请启动 Docker Desktop"
    exit 1
fi

echo "✓ Docker 已准备就绪"
echo ""

# 检查是否需要构建
if [ ! -f "$PROJECT_DIR/target/mock-service-backend-1.0.0.jar" ]; then
    echo "⚠️  JAR 文件不存在，需要构建..."
    echo ""
    echo "为避免本地 Java 版本兼容性问题，将使用 Docker 进行构建。"
    echo "请确保网络连接正常..."
    echo ""

    read -p "是否继续？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        cd "$PROJECT_DIR"
        bash build-docker.sh
    else
        echo "已取消构建。可以稍后手动运行：bash build-docker.sh"
        exit 1
    fi
fi

echo ""
echo "════════════════════════════════════════════════════════════"
echo "  启动 Docker 容器..."
echo "════════════════════════════════════════════════════════════"
echo ""

cd "$PROJECT_DIR"

# 检查 docker-compose 文件
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 错误: docker-compose.yml 未找到"
    exit 1
fi

# 启动容器
docker-compose up -d

echo ""
echo "✓ 容器已启动"
echo ""

# 等待应用启动
echo "⏳ 等待应用启动（约 15-30 秒）..."
sleep 5

# 检查应用状态
for i in {1..30}; do
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo "✓ 应用已就绪！"
        break
    fi
    echo -n "."
    sleep 1
done

echo ""
echo ""
echo "════════════════════════════════════════════════════════════"
echo "  启动完成！"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "📊 服务信息："
echo "  • API 地址: http://localhost:8080/api"
echo "  • H2 控制台: http://localhost:8080/api/h2-console"
echo ""
echo "🔧 常用命令："
echo "  • 查看日志: docker-compose logs -f mock-service"
echo "  • 停止服务: docker-compose down"
echo "  • 重启服务: docker-compose restart"
echo ""
echo "📖 更多信息: 查看 DOCKER_DEPLOYMENT.md"
echo ""
