-- 创建数据库 (如果使用 docker-compose，数据库已由 MYSQL_DATABASE 创建)
-- CREATE DATABASE IF NOT EXISTS mock_service_db;
-- USE mock_service_db;

-- 创建环境表
CREATE TABLE IF NOT EXISTS environment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    base_url VARCHAR(500),
    headers JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建环境变量表
CREATE TABLE IF NOT EXISTS environment_variable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    key_name VARCHAR(255) NOT NULL,
    value TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (environment_id) REFERENCES environment(id) ON DELETE CASCADE,
    UNIQUE KEY uk_env_var (environment_id, key_name)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建 API 信息表
CREATE TABLE IF NOT EXISTS api_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    method VARCHAR(20) NOT NULL,
    path VARCHAR(500) NOT NULL,
    description TEXT,
    request_body_schema JSON,
    response_schema JSON,
    examples JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_api (method, path)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建 Mock 配置表
CREATE TABLE IF NOT EXISTS mock_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_info_id BIGINT NOT NULL,
    environment_id BIGINT,
    mock_data JSON NOT NULL,
    delay_ms INT DEFAULT 0,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (api_info_id) REFERENCES api_info(id) ON DELETE CASCADE,
    FOREIGN KEY (environment_id) REFERENCES environment(id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建 Mock 历史表
CREATE TABLE IF NOT EXISTS mock_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_path VARCHAR(500),
    method VARCHAR(20),
    request_body TEXT,
    response_body TEXT,
    status_code INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建测试场景表
CREATE TABLE IF NOT EXISTS test_scenario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建测试步骤表
CREATE TABLE IF NOT EXISTS test_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT NOT NULL,
    step_name VARCHAR(255) NOT NULL,
    api_path VARCHAR(500) NOT NULL,
    method VARCHAR(20) NOT NULL,
    request_body TEXT,
    expected_response JSON,
    assertions JSON,
    order_num INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (scenario_id) REFERENCES test_scenario(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建测试执行历史表
CREATE TABLE IF NOT EXISTS test_execution_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scenario_id BIGINT,
    status VARCHAR(50),
    total_steps INT,
    passed_steps INT,
    failed_steps INT,
    execution_time_ms BIGINT,
    error_message TEXT,
    execution_details JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建索引以提高查询性能
CREATE INDEX idx_api_info_path ON api_info(path);
CREATE INDEX idx_api_info_method ON api_info(method);
CREATE INDEX idx_mock_config_api ON mock_config(api_info_id);
CREATE INDEX idx_mock_config_env ON mock_config(environment_id);
CREATE INDEX idx_mock_history_created ON mock_history(created_at);
CREATE INDEX idx_test_step_scenario ON test_step(scenario_id);
CREATE INDEX idx_test_execution_scenario ON test_execution_history(scenario_id);
CREATE INDEX idx_test_execution_created ON test_execution_history(created_at);
CREATE INDEX idx_env_var_env ON environment_variable(environment_id);

-- 插入默认数据
INSERT IGNORE INTO environment (name, description, base_url) VALUES
('开发环境', '本地开发环境', 'http://localhost:8080/api'),
('测试环境', '集成测试环境', 'http://test-server:8080/api'),
('生产环境', '生产环境', 'http://prod-server:8080/api');
