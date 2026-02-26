# 🐳 Docker 快速启动指南

## 概述

Mock Service 已完整配置 Docker 部署，只需简单的命令即可启动。

## ⚡ 快速开始

### Windows 用户
```batch
# 直接运行启动脚本
start-docker.bat
```

### Linux/Mac 用户
```bash
# 赋予执行权限
chmod +x start-docker.sh

# 运行启动脚本
./start-docker.sh
```

## 🎯 手动启动步骤

如果脚本无法运行，可以手动执行以下步骤：

### 第 1 步：检查 Docker 状态
```bash
docker --version
docker-compose --version
```

### 第 2 步：构建应用（首次需要，需要网络连接）
如果还没有 JAR 文件，可以使用以下方式之一：

**选项 A：使用构建脚本（自动）**
```bash
# Windows
build-docker.bat

# Linux/Mac
bash build-docker.sh
```

**选项 B：手动 Maven 构建**
```bash
# 如果本地环境支持，可以直接构建
mvn clean package -DskipTests
```

### 第 3 步：启动容器
```bash
# 启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 第 4 步：验证服务
```bash
# 检查 API 健康状态
curl http://localhost:8080/api/health

# 应该返回类似结果
# {"status":"UP","details":...}
```

## 📊 服务地址

启动成功后，可以通过以下地址访问：

| 项目 | URL | 说明 |
|------|-----|------|
| API 根路径 | http://localhost:8080/api | 所有 API 的基础 URL |
| 健康检查 | http://localhost:8080/api/health | 查看应用状态 |
| H2 数据库控制台 | http://localhost:8080/api/h2-console | 数据库管理界面（H2 模式） |

## 🚀 推荐工作流程

### 1️⃣ 首次启动
```bash
# 一键启动（推荐）
start-docker.sh  # 或 start-docker.bat

# 等待应用启动完成（通常 15-30 秒）
```

### 2️⃣ 日常开发
```bash
# 启动开发环境
docker-compose up -d

# 查看实时日志
docker-compose logs -f mock-service

# 在另一个终端执行 API 测试
curl http://localhost:8080/api/health
```

### 3️⃣ 停止服务
```bash
# 停止但保留容器和数据
docker-compose stop

# 完全移除（删除容器）
docker-compose down

# 移除所有数据卷
docker-compose down -v
```

## 🔍 故障排查

### ❌ 问题：容器启动失败

```bash
# 1. 查看详细错误
docker-compose logs

# 2. 检查端口占用
netstat -an | grep 8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# 3. 重新构建
docker-compose up -d --build

# 4. 清理后重试
docker-compose down -v
docker-compose up -d
```

### ❌ 问题：无法连接数据库

```bash
# H2 数据库
# - 删除 data 目录中的文件
rm -rf data/mockdb*

# MySQL 数据库
# - 检查 MySQL 容器
docker-compose logs mysql

# - 重启 MySQL
docker-compose restart mysql
```

### ❌ 问题：端口 8080 被占用

编辑 `docker-compose.yml`，修改：
```yaml
mock-service:
  ports:
    - "8081:8080"  # 改为 8081 或其他端口
```

## 📝 常用 Docker 命令

```bash
# 启动/停止/重启
docker-compose up -d              # 启动
docker-compose stop               # 停止
docker-compose restart            # 重启
docker-compose down               # 停止并移除

# 日志相关
docker-compose logs               # 查看日志
docker-compose logs -f mock-service # 实时日志
docker-compose logs --tail 50     # 查看最后 50 行

# 容器管理
docker-compose ps                 # 查看容器状态
docker-compose exec mock-service bash  # 进入容器

# 数据管理
docker volume ls                  # 列出所有卷
docker volume rm <volume-name>    # 删除卷
```

## 🔧 配置修改

### 修改端口
编辑 `docker-compose.yml`：
```yaml
mock-service:
  ports:
    - "8081:8080"  # 主机:容器
```

### 使用 MySQL 而不是 H2
编辑 `docker-compose.yml`，取消注释 MySQL 部分。

### 修改日志级别
编辑 `docker-compose.yml`：
```yaml
environment:
  LOGGING_LEVEL_COM_MOCK_SERVICE: DEBUG  # 改为 INFO、WARN 等
```

## 📚 文件说明

| 文件 | 说明 |
|------|------|
| `Dockerfile` | 应用镜像构建配置 |
| `docker-compose.yml` | 容器编排定义 |
| `.dockerignore` | 构建时忽略的文件 |
| `scripts/init.sql` | MySQL 初始化脚本 |
| `DOCKER_DEPLOYMENT.md` | 详细部署文档 |
| `start-docker.sh/bat` | 启动脚本 |
| `build-docker.sh/bat` | 构建脚本 |

## ✅ 验证清单

启动成功的标志：

- [ ] `docker-compose ps` 显示 mock-service 为 `Up`
- [ ] `curl http://localhost:8080/api/health` 返回 200 状态码
- [ ] 查看 H2 控制台可以看到数据库表
- [ ] 日志中没有明显的错误信息

## 💡 提示

- **首次启动较慢**：这是正常的，应用正在初始化数据库
- **使用 H2 用于开发**：H2 无需额外安装，快速启动
- **使用 MySQL 用于生产**：MySQL 更稳定，适合长期运行
- **定期备份**：如果使用数据库，请定期备份重要数据

## 🆘 需要帮助？

1. 查看详细文档：`DOCKER_DEPLOYMENT.md`
2. 查看容器日志：`docker-compose logs`
3. 检查快速启动指南：`QUICK_START.md`
4. 运行健康检查：`curl http://localhost:8080/api/health`

## 🎉 下一步

- [ ] 启动应用：`start-docker.sh` 或 `start-docker.bat`
- [ ] 访问 API：http://localhost:8080/api
- [ ] 创建第一个 Mock：见 API 文档
- [ ] 配置数据库（如需）：编辑 `docker-compose.yml`

---

**祝您使用愉快！** 🚀
