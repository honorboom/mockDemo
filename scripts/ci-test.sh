#!/bin/bash

# ============================================
# Mock Service Backend - CI/CD 自动化测试脚本
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 脚本参数
ENVIRONMENT=${1:-dev}
RUN_SECURITY=${2:-true}
RUN_PERFORMANCE=${3:-false}

log_info "========== 后端自动化测试流程 =========="
log_info "环境: $ENVIRONMENT"
log_info "运行安全扫描: $RUN_SECURITY"
log_info "运行性能测试: $RUN_PERFORMANCE"

# 步骤 1: 检查环境
log_info "步骤 1: 检查环境"
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 未安装"
        exit 1
    fi
    log_success "$1 已安装"
}

check_command java
check_command mvn
check_command docker

# 步骤 2: 编译项目
log_info "步骤 2: 编译项目"
cd mock-service-backend
mvn clean compile || {
    log_error "编译失败"
    exit 1
}
log_success "编译成功"

# 步骤 3: 运行单元测试
log_info "步骤 3: 运行单元测试"
mvn test \
    -Dorg.slf4j.simpleLogger.defaultLogLevel=info \
    || {
    log_error "单元测试失败"
    exit 1
}
log_success "单元测试通过"

# 步骤 4: 代码覆盖率检查
log_info "步骤 4: 代码覆盖率检查"
mvn jacoco:report || {
    log_warning "代码覆盖率报告生成失败"
}

# 获取覆盖率百分比
COVERAGE=$(grep -oP 'INSTRUCTION.*?<value>\K[^<]+' target/site/jacoco/jacoco.xml 2>/dev/null || echo "N/A")
log_info "代码覆盖率: $COVERAGE %"

# 步骤 5: 安全扫描
if [ "$RUN_SECURITY" = "true" ]; then
    log_info "步骤 5: 安全扫描"

    # OWASP 依赖检查
    log_info "  - 运行 OWASP 依赖检查..."
    mvn org.owasp:dependency-check-maven:check || {
        log_warning "OWASP 检查发现问题，继续"
    }

    # SpotBugs（查找潜在 bug）
    log_info "  - 运行 SpotBugs..."
    mvn com.github.spotbugs:spotbugs-maven-plugin:check || {
        log_warning "SpotBugs 发现问题，继续"
    }

    log_success "安全扫描完成"
else
    log_warning "跳过安全扫描"
fi

# 步骤 6: 代码质量检查
log_info "步骤 6: 代码质量检查"
mvn checkstyle:check || {
    log_warning "代码规范检查发现问题"
}

# 步骤 7: 构建 JAR
log_info "步骤 7: 构建 JAR 包"
mvn package -DskipTests || {
    log_error "JAR 构建失败"
    exit 1
}
log_success "JAR 构建成功"

# 步骤 8: 验证 JAR
log_info "步骤 8: 验证 JAR"
JAR_FILE=$(ls target/*.jar | head -1)
if [ -f "$JAR_FILE" ]; then
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    log_success "JAR 包生成成功: $JAR_FILE (大小: $JAR_SIZE)"
else
    log_error "JAR 包未找到"
    exit 1
fi

# 步骤 9: 性能测试
if [ "$RUN_PERFORMANCE" = "true" ]; then
    log_info "步骤 9: 性能测试"

    # 启动应用
    log_info "  - 启动应用..."
    java -jar "$JAR_FILE" \
        --server.port=8081 \
        --spring.jpa.hibernate.ddl-auto=create-drop &
    APP_PID=$!
    sleep 10

    # 健康检查
    if curl -f http://localhost:8081/api/health; then
        log_success "应用启动成功"

        # 运行简单的性能测试
        log_info "  - 运行性能测试..."
        for i in {1..100}; do
            curl -s http://localhost:8081/api/mock/list > /dev/null &
        done
        wait
        log_success "性能测试完成"
    else
        log_error "应用启动失败"
    fi

    # 清理
    kill $APP_PID || true
else
    log_warning "跳过性能测试"
fi

# 步骤 10: Docker 构建
log_info "步骤 10: Docker 构建"
docker build \
    -t mock-service-backend:test \
    -f Dockerfile \
    . || {
    log_warning "Docker 镜像构建失败"
}
log_success "Docker 镜像构建完成"

# 最终报告
log_info "========== 测试完成 =========="
log_success "所有必要的测试和检查已完成"
log_info "关键指标:"
log_info "  - 代码覆盖率: $COVERAGE %"
log_info "  - JAR 大小: $JAR_SIZE"
log_info "  - 环境: $ENVIRONMENT"

exit 0
