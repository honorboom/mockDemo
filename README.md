# Mock Service Backend - 启动指南

## 项目概述

Mock Service Backend 是基于 Spring Boot 3.2 的 Mock 服务平台后端，提供动态 Mock 数据模拟、API 代理转发、Proto 文件管理、自动化测试执行等功能。

**技术栈：** Java 17 / Spring Boot 3.2 / Spring Data JPA / Maven / H2 或 MySQL

## 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 必需 |
| Maven | 3.6+ | 必需 |
| MySQL | 8.0+ | 生产环境必需，开发可用 H2 替代 |

## 快速启动

### 1. 克隆项目

```bash
git clone https://github.com/honorboom/mockDemo.git mock-service-backend
cd mock-service-backend
```

### 2. 选择数据库模式

项目通过 `src/main/resources/application.yml` 中的 `spring.profiles.active` 切换数据库：

```yaml
spring:
  profiles:
    active: h2     # 开发模式，使用内嵌 H2 数据库，无需额外安装
    # active: mysql  # 生产模式，使用 MySQL 数据库
```

#### H2 模式（开发推荐，开箱即用）

无需额外配置，数据文件自动存储在 `./data/mockdb.mv.db`。

启动后可访问 H2 控制台：
- 地址：http://localhost:8080/api/h2-console
- JDBC URL：`jdbc:h2:file:./data/mockdb`
- 用户名：`sa`，密码为空

#### MySQL 模式（生产推荐）

1) 创建数据库：

```sql
CREATE DATABASE mock_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2) 修改 `src/main/resources/application-mysql.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mock_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password   # 修改为你的密码
```

3) 将 `application.yml` 中的 `spring.profiles.active` 改为 `mysql`。

### 3. 编译并启动

**方式一：Maven 直接运行（开发推荐）**

```bash
mvn clean compile
mvn spring-boot:run
```

**方式二：打包为 JAR 运行**

```bash
mvn clean package -DskipTests
java -jar target/mock-service-backend-1.0.0.jar
```

**方式三：IDE 运行**

在 IntelliJ IDEA 或 VS Code 中打开项目，运行 `MockServiceApplication.java` 的 `main` 方法。

**方式四：Docker 运行**

```bash
docker compose up -d
```

默认使用 H2 数据库，如需 MySQL 请修改 `docker-compose.yml` 中的环境变量。

### 4. 验证服务

服务启动后默认监听 `http://localhost:8080`，API 根路径为 `/api`。

```bash
# 测试 Mock 配置接口
curl http://localhost:8080/api/mock-configs

# 测试 API 信息接口
curl http://localhost:8080/api/api-info
```

## API 端点一览

| 模块 | 端点 | 方法 | 说明 |
|------|------|------|------|
| Mock 配置 | `/api/mock-configs` | GET/POST/PUT/DELETE | Mock 规则的增删改查 |
| 动态 Mock | `/api/dynamic-mock/**` | GET/POST | 根据配置返回 Mock 数据 |
| API 信息 | `/api/api-info` | GET/POST/PUT/DELETE | API 接口信息管理 |
| API 代理 | `/api/proxy/**` | GET/POST | 代理转发真实 API 请求 |
| 环境管理 | `/api/environment` | GET/POST/PUT/DELETE | 多环境配置管理 |
| Proto 管理 | `/api/proto-files` | GET/POST/DELETE | Proto 文件上传与管理 |
| 测试场景 | `/api/test-scenarios` | GET/POST/PUT/DELETE | 测试场景管理 |
| 测试执行 | `/api/test-execution` | POST | 执行测试场景 |
| 调用历史 | `/api/mock-history` | GET | Mock 调用历史记录 |
| 执行历史 | `/api/test-history` | GET | 测试执行历史记录 |

## 项目结构

```
mock-service-backend/
├── src/main/java/com/mock/service/
│   ├── MockServiceApplication.java    # 启动类
│   ├── config/                        # 配置类
│   ├── controller/                    # 控制器层
│   │   ├── MockConfigController.java      # Mock 配置管理
│   │   ├── DynamicMockController.java     # 动态 Mock 响应
│   │   ├── ApiInfoController.java         # API 信息管理
│   │   ├── ApiProxyController.java        # API 代理转发
│   │   ├── EnvironmentController.java     # 环境管理
│   │   ├── ProtoFileController.java       # Proto 文件管理
│   │   ├── TestScenarioController.java    # 测试场景管理
│   │   ├── TestExecutionController.java   # 测试执行
│   │   ├── MockHistoryController.java     # Mock 调用历史
│   │   └── TestExecutionHistoryController.java  # 测试执行历史
│   ├── dto/                           # 数据传输对象
│   ├── entity/                        # JPA 实体类
│   ├── repository/                    # 数据访问层
│   ├── service/                       # 业务逻辑层
│   └── util/                          # 工具类
├── src/main/resources/
│   ├── application.yml                # 主配置文件
│   ├── application-h2.yml             # H2 数据库配置
│   └── application-mysql.yml          # MySQL 数据库配置
├── Dockerfile                         # Docker 构建文件
├── docker-compose.yml                 # Docker Compose 编排
├── Jenkinsfile                        # CI/CD 流水线
└── pom.xml                            # Maven 依赖配置
```

## 关键配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `server.port` | 8080 | 服务端口 |
| `server.servlet.context-path` | /api | API 根路径 |
| `spring.profiles.active` | mysql | 数据库模式（h2/mysql） |
| `spring.jpa.hibernate.ddl-auto` | update | 表结构自动更新 |
| `logging.level.com.mock.service` | DEBUG | 应用日志级别 |

## 常见问题

**端口 8080 被占用**

修改 `application.yml` 中 `server.port` 为其他端口，或启动时指定：
```bash
java -jar target/mock-service-backend-1.0.0.jar --server.port=8081
```

**MySQL 连接失败**

检查 MySQL 服务是否启动，以及 `application-mysql.yml` 中的用户名密码是否正确：
```bash
mysql -u root -p -e "SELECT VERSION();"
```

**Maven 依赖下载失败**

清除本地缓存重新下载：
```bash
mvn clean install -DskipTests
```

**Java 版本不符**

确认 JDK 版本为 17+：
```bash
java -version
```
