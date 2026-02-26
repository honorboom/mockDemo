# Docker 部署配置指南

## 📋 概述

Mock Service 已配置好 Docker 部署。本指南说明如何启动容器。

---

## 🚀 快速启动（推荐）

### 前置要求
- Docker Desktop 已安装并运行
- 网络连接正常（用于首次拉取镜像）

### 启动步骤

#### 1️⃣ 方案 A：使用 H2 数据库（快速启动，适合开发/测试）

```bash
# 进入项目目录
cd mock-service-backend

# 启动应用（使用 H2 内存数据库）
docker-compose up -d
```

**验证服务是否启动：**
```bash
# 检查容器状态
docker-compose ps

# 查看日志
docker-compose logs -f mock-service

# 健康检查
curl http://localhost:8080/api/health
```

#### 2️⃣ 方案 B：使用 MySQL 数据库（生产推荐）

编辑 `docker-compose.yml`，取消注释 MySQL 服务部分，然后：

```bash
docker-compose up -d
```

---

## 📂 文件说明

| 文件 | 说明 |
|------|------|
| `Dockerfile` | 应用镜像构建配置（多阶段构建） |
| `docker-compose.yml` | 容器编排配置，包含 H2 和 MySQL 两种方案 |
| `.dockerignore` | Docker 构建忽略列表 |
| `scripts/init.sql` | MySQL 数据库初始化脚本 |

---

## 🔧 常用命令

```bash
# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 查看实时日志
docker-compose logs -f mock-service

# 重启容器
docker-compose restart mock-service

# 进入容器
docker-compose exec mock-service bash

# 查看容器资源使用
docker stats

# 清理所有容器和卷
docker-compose down -v
```

---

## 📊 容器信息

启动后，应用可以通过以下地址访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| 应用 API | `http://localhost:8080/api` | Mock 服务后端 |
| H2 控制台 | `http://localhost:8080/api/h2-console` | 仅 H2 数据库模式有效 |
| MySQL | `localhost:3306` | 仅 MySQL 模式有效 |

### H2 控制台连接

- **JDBC URL**: `jdbc:h2:file:./data/mockdb`
- **用户名**: `sa`
- **密码**: 留空

---

## 🐛 常见问题

### Q1: 容器无法启动

**检查步骤：**
```bash
# 1. 查看错误日志
docker-compose logs mock-service

# 2. 检查端口占用
netstat -an | grep 8080

# 3. 检查 Docker 状态
docker ps -a

# 4. 重新构建镜像
docker-compose up -d --build
```

### Q2: 无法连接到数据库

**对于 H2 数据库：**
- 检查 `./data` 目录是否存在且有读写权限
- 尝试删除 `./data/mockdb*` 文件后重启

**对于 MySQL：**
```bash
# 检查 MySQL 容器状态
docker-compose ps mysql

# 查看 MySQL 日志
docker-compose logs mysql

# 重启 MySQL
docker-compose restart mysql
```

### Q3: 应用启动很慢

- 这是正常的，首次启动会初始化数据库
- 查看日志确认：`Application ready in XXX seconds`

### Q4: 端口 8080 已被占用

编辑 `docker-compose.yml`，修改端口映射：
```yaml
ports:
  - "8081:8080"  # 改为 8081
```

---

## 🔌 API 示例

### 检查健康状态
```bash
curl http://localhost:8080/api/health
```

### 获取所有 Mock 配置
```bash
curl http://localhost:8080/api/mock-config
```

### 创建 Mock 配置
```bash
curl -X POST http://localhost:8080/api/mock-config \
  -H "Content-Type: application/json" \
  -d '{
    "name": "获取用户信息",
    "path": "/api/users/1",
    "method": "GET",
    "statusCode": 200,
    "responseBody": "{\"id\": 1, \"name\": \"Test User\"}"
  }'
```

---

## 📝 环境变量配置

可以在 `docker-compose.yml` 中修改以下环境变量：

```yaml
environment:
  SPRING_PROFILES_ACTIVE: h2          # 激活的配置（h2 或 mysql）
  SPRING_JPA_HIBERNATE_DDL_AUTO: update  # Hibernate DDL 策略
  LOGGING_LEVEL_COM_MOCK_SERVICE: DEBUG  # 日志级别
```

---

## 🔒 生产部署

### 安全建议

1. **使用 MySQL 而不是 H2**（H2 仅用于开发）
2. **配置强密码**
3. **限制 API 访问**（使用反向代理、防火墙等）
4. **启用 HTTPS**
5. **定期备份数据库**

### 部署示例

```bash
# 在生产环境启动
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 查看数据卷
docker volume ls

# 备份数据库
docker-compose exec mysql mysqldump -uroot -proot123 mock_service_db > backup.sql
```

---

## 📚 相关文档

- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [Spring Boot Docker 部署](https://spring.io/guides/gs/spring-boot-docker/)
- [项目快速启动指南](./QUICK_START.md)

---

## ✨ 下一步

- [ ] 运行 `docker-compose up -d`
- [ ] 验证应用启动成功
- [ ] 通过 API 创建第一个 Mock 配置
- [ ] 配置您的测试环境

---

**需要帮助？** 查看容器日志：`docker-compose logs -f`
