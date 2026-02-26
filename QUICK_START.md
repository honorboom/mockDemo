# Mock Service Backend - 快速启动指南

## 项目概述

Mock Service Backend 是一个基于 Spring Boot 3.2 的高效 Mock 服务平台，提供动态数据模拟、API 代理、测试执行等功能。

**技术栈：**
- Java 17
- Spring Boot 3.2
- Spring Data JPA
- Maven
- H2/MySQL 数据库

---

## 环境要求

### 必需条件
- **JDK 17** 或更高版本
- **Maven 3.6+**

### 可选条件
- **MySQL 8.0+** (如需使用 MySQL 而非默认 H2 数据库)
- **IDE**: IntelliJ IDEA 或 VS Code

---

## 快速启动步骤

### 方案 A：使用 H2 数据库（推荐 - 开发/测试快速体验）

#### 1️⃣ 克隆/进入项目
```bash
cd mock-service-backend
```

#### 2️⃣ 编译项目
```bash
mvn clean compile
```

#### 3️⃣ 启动应用
```bash
mvn spring-boot:run
```

或者打包后运行：
```bash
mvn clean package -DskipTests
java -jar target/mock-service-backend-1.0.0.jar
```

#### 4️⃣ 验证服务
```bash
# 健康检查
curl http://localhost:8080/api/health

# 查看 H2 控制台
# 访问: http://localhost:8080/api/h2-console
# JDBC URL: jdbc:h2:file:./data/mockdb
# 用户名: sa
# 密码: (空)
```

---

### 方案 B：使用 MySQL 数据库（生产推荐）

#### 1️⃣ 启动 MySQL 服务
```bash
# Windows
net start MySQL80

# Linux/Mac
sudo service mysql start
# 或
brew services start mysql
```

#### 2️⃣ 初始化数据库
```bash
mysql -u root -p < ../sql/init.sql
```

或手动执行：
```sql
CREATE DATABASE mock_service_db;
```

#### 3️⃣ 编辑配置文件

修改 `src/main/resources/application.yml`，激活 mysql 配置：

```yaml
spring:
  profiles:
    active: mysql  # 改为 mysql
```

#### 4️⃣ 配置数据库连接

编辑 `src/main/resources/application-mysql.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mock_service_db?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 5️⃣ 启动应用
```bash
mvn spring-boot:run
```

#### 6️⃣ 验证服务
```bash
curl http://localhost:8080/api/health
```

---

## 关键配置说明

### 服务端口和上下文
```yaml
server:
  port: 8080              # 应用端口
  servlet:
    context-path: /api    # API 根路径
```

### 日志级别
```yaml
logging:
  level:
    com.mock.service: DEBUG      # 应用日志
    org.springframework.web: INFO # Web 框架日志
```

### JPA/Hibernate
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update    # 自动创建/更新表结构
    show-sql: false       # 关闭 SQL 日志（性能考虑）
```

---

## 常用 API 端点

| 功能 | 端点 | 方法 |
|------|------|------|
| 获取 API 信息 | `/api/api-info` | GET |
| 代理 API 请求 | `/api/proxy/**` | GET/POST |
| 动态 Mock 数据 | `/api/dynamic-mock/**` | GET/POST |
| 环境管理 | `/api/environment` | GET/POST |
| 测试执行 | `/api/test-execution` | POST |
| 执行历史 | `/api/test-history` | GET |

---

## 数据库管理

### H2 数据库
- **文件位置**: `./data/mockdb.mv.db`
- **管理界面**: http://localhost:8080/api/h2-console
- **优点**: 无需安装，快速启动，适合开发
- **缺点**: 不适合生产环境

### MySQL 数据库
- **初始化脚本**: `../sql/init.sql`
- **优点**: 生产级别，可靠性高
- **缺点**: 需要额外部署

---

## 故障排查

### 问题 1: 端口 8080 已被占用
```bash
# 修改配置文件，改用其他端口
server:
  port: 8081
```

### 问题 2: MySQL 连接失败
```bash
# 检查 MySQL 服务状态
mysql -u root -p -e "SELECT VERSION();"

# 验证连接字符串
jdbc:mysql://localhost:3306/mock_service_db?useSSL=false&serverTimezone=UTC
```

### 问题 3: Maven 依赖下载失败
```bash
# 清除本地缓存重新下载
mvn clean install -DskipTests
```

### 问题 4: Java 版本不符
```bash
# 检查 Java 版本
java -version

# 需要 Java 17+
```

---

## 开发工作流

### 编译和测试
```bash
# 编译
mvn clean compile

# 运行单元测试
mvn test

# 跳过测试打包
mvn clean package -DskipTests
```

### 生成可执行 JAR
```bash
mvn clean package -DskipTests
# 生成: target/mock-service-backend-1.0.0.jar
```

### 运行 JAR
```bash
java -jar target/mock-service-backend-1.0.0.jar
```

### 使用 IDE 运行
1. 在 IDE 中打开项目
2. 找到 `src/main/java/com/mock/service/MockServiceApplication.java`
3. 右键运行 `Run 'MockServiceApplication.main()'`

---

## 生产部署

### 1. 打包应用
```bash
mvn clean package -DskipTests -P prod
```

### 2. 部署到服务器
```bash
scp target/mock-service-backend-1.0.0.jar user@server:/app/
ssh user@server

cd /app
java -jar mock-service-backend-1.0.0.jar \
  --spring.profiles.active=mysql \
  --spring.datasource.url=jdbc:mysql://db-host:3306/mock_service_db \
  --spring.datasource.username=app_user \
  --spring.datasource.password=secure_password \
  --server.port=8080
```

### 3. 使用 systemd 管理服务（Linux）
```bash
# 创建服务文件: /etc/systemd/system/mock-service.service
[Unit]
Description=Mock Service Backend
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/app
ExecStart=/usr/bin/java -jar mock-service-backend-1.0.0.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target

# 启动服务
sudo systemctl daemon-reload
sudo systemctl start mock-service
sudo systemctl enable mock-service
```

---

## 监控和日志

### 实时日志
```bash
# 应用启动时在控制台查看日志
tail -f logs/application.log

# 如使用 JAR 运行，重定向日志
java -jar mock-service-backend-1.0.0.jar > application.log 2>&1 &
```

### 健康检查
```bash
# 默认端点（如启用 Actuator）
curl http://localhost:8080/api/actuator/health
```

---

## 常见命令速查表

| 命令 | 说明 |
|------|------|
| `mvn clean compile` | 清除并编译项目 |
| `mvn spring-boot:run` | 运行应用 |
| `mvn clean package -DskipTests` | 打包为 JAR |
| `java -jar app.jar` | 运行 JAR |
| `mvn test` | 运行测试 |
| `mvn clean install` | 清除、编译、测试、打包、安装 |

---

## 相关资源

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Spring Data JPA 文档](https://spring.io/projects/spring-data-jpa)
- [Maven 官方文档](https://maven.apache.org/)
- 项目数据库初始化脚本：`../sql/init.sql`

---

## 更新日志

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-02 | 初始版本发布 |

---

**需要帮助？** 查看项目主 README.md 或联系开发团队。
