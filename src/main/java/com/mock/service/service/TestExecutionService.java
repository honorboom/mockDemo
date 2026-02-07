package com.mock.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.service.dto.StepExecutionResult;
import com.mock.service.dto.TestExecutionResult;
import com.mock.service.entity.TestScenario;
import com.mock.service.entity.TestStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试执行服务类
 * <p>
 * 负责执行自动化测试场景，包括：
 * <ul>
 *   <li>场景状态验证</li>
 *   <li>测试步骤顺序执行</li>
 *   <li>环境变量替换（URL、参数、请求头、请求体）</li>
 *   <li>HTTP请求发送（支持GET/POST/PUT/DELETE等所有方法）</li>
 *   <li>响应状态码验证</li>
 *   <li>响应内容断言</li>
 *   <li>执行结果统计和保存</li>
 * </ul>
 * </p>
 * <p>
 * 核心流程：
 * <ol>
 *   <li>加载场景和步骤信息</li>
 *   <li>验证场景启用状态</li>
 *   <li>按stepOrder顺序执行每个步骤</li>
 *   <li>每个步骤执行前应用延迟（如果配置）</li>
 *   <li>替换环境变量（如果指定环境）</li>
 *   <li>发送HTTP请求</li>
 *   <li>验证响应状态码和断言</li>
 *   <li>汇总执行结果并保存历史</li>
 * </ol>
 * </p>
 *
 * @author Mock Service Team
 * @version 1.0
 * @since 2024-01-01
 * @see TestScenarioService
 * @see EnvironmentService
 * @see TestExecutionHistoryService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TestExecutionService {

    /** 测试场景服务，用于获取场景和步骤信息 */
    private final TestScenarioService scenarioService;

    /** 环境变量服务，用于变量替换和baseUrl拼接 */
    private final EnvironmentService environmentService;

    /** 测试执行历史服务，用于保存执行记录 */
    private final TestExecutionHistoryService historyService;

    /** HTTP客户端，用于发送请求 */
    private final RestTemplate restTemplate;

    /** JSON序列化/反序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 执行测试场景（不带环境变量）
     * <p>
     * 使用场景中配置的原始URL和参数执行测试，不进行环境变量替换。
     * </p>
     *
     * @param scenarioId 场景ID
     * @return 测试执行结果，包含整体成功状态和每个步骤的详细结果
     */
    public TestExecutionResult executeScenario(Long scenarioId) {
        return executeScenario(scenarioId, null);
    }

    /**
     * 执行测试场景（带环境变量）
     * <p>
     * 这是核心执行方法，完整流程包括：
     * <ol>
     *   <li>验证场景启用状态</li>
     *   <li>加载所有测试步骤</li>
     *   <li>按顺序执行每个步骤（支持延迟）</li>
     *   <li>应用环境变量替换（如果指定环境）</li>
     *   <li>统计通过/失败步骤数</li>
     *   <li>保存执行历史记录</li>
     * </ol>
     * </p>
     *
     * @param scenarioId 场景ID
     * @param environmentId 环境ID，如果为null则不进行变量替换
     * @return 测试执行结果，包含整体状态、步骤结果、耗时等信息
     */
    public TestExecutionResult executeScenario(Long scenarioId, Long environmentId) {
        long startTime = System.currentTimeMillis();

        TestExecutionResult result = new TestExecutionResult();
        result.setScenarioId(scenarioId);

        try {
            // 获取场景信息
            TestScenario scenario = scenarioService.getScenario(scenarioId);
            result.setScenarioName(scenario.getName());

            log.info("╔═══════════════════════════════════════════════════════════════");
            log.info("║ 🚀 开始执行测试场景");
            log.info("║ 场景ID: {}", scenarioId);
            log.info("║ 场景名称: {}", scenario.getName());
            if (environmentId != null) {
                com.mock.service.entity.Environment environment = environmentService.getEnvironmentById(environmentId);
                if (environment != null) {
                    log.info("║ 使用环境: {} (ID: {})", environment.getName(), environmentId);
                }
            } else {
                log.info("║ 使用环境: (无)");
            }
            log.info("╚═══════════════════════════════════════════════════════════════");

            if (!scenario.getEnabled()) {
                result.setSuccess(false);
                result.setErrorMessage("场景未启用");
                log.warn("⚠️  场景未启用，跳过执行");
                return result;
            }

            // 获取所有步骤
            List<TestStep> steps = scenarioService.getScenarioSteps(scenarioId);
            result.setTotalSteps(steps.size());

            if (steps.isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("场景没有测试步骤");
                log.warn("⚠️  场景没有测试步骤");
                return result;
            }

            log.info("📋 共 {} 个测试步骤待执行", steps.size());
            log.info("");

            // 执行所有步骤
            List<StepExecutionResult> stepResults = new ArrayList<>();
            int passedCount = 0;
            int failedCount = 0;

            for (TestStep step : steps) {
                // 执行延迟
                if (step.getDelay() != null && step.getDelay() > 0) {
                    log.info("⏱️  等待 {}ms...", step.getDelay());
                    Thread.sleep(step.getDelay());
                }

                StepExecutionResult stepResult = executeStep(step, environmentId);
                stepResults.add(stepResult);

                if (stepResult.getSuccess()) {
                    passedCount++;
                } else {
                    failedCount++;
                }
                log.info("");
            }

            result.setStepResults(stepResults);
            result.setPassedSteps(passedCount);
            result.setFailedSteps(failedCount);
            result.setSuccess(failedCount == 0);

            // 打印场景执行总结
            log.info("╔═══════════════════════════════════════════════════════════════");
            log.info("║ 📊 测试场景执行完成");
            log.info("║ 场景名称: {}", scenario.getName());
            log.info("║ 总步骤数: {}", steps.size());
            log.info("║ ✅ 通过: {}", passedCount);
            log.info("║ ❌ 失败: {}", failedCount);
            log.info("║ 总耗时: {}ms", System.currentTimeMillis() - startTime);
            if (result.getSuccess()) {
                log.info("║ 最终结果: ✅ 成功");
            } else {
                log.info("║ 最终结果: ❌ 失败");
            }
            log.info("╚═══════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("❌ 执行场景异常: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setDuration(System.currentTimeMillis() - startTime);
        }

        // 保存执行历史
        try {
            String environmentName = null;
            if (environmentId != null) {
                com.mock.service.entity.Environment environment = environmentService.getEnvironmentById(environmentId);
                if (environment != null) {
                    environmentName = environment.getName();
                }
            }
            historyService.saveHistory(result, environmentId, environmentName);
        } catch (Exception e) {
            log.error("保存执行历史失败: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * 执行单个测试步骤
     * <p>
     * 这是核心的步骤执行逻辑，包含完整的请求发送和响应验证流程：
     * <ol>
     *   <li>环境变量替换：如果指定了environmentId，会替换URL、params、headers、body中的变量</li>
     *   <li>相对路径处理：如果URL以/开头，会自动拼接环境的baseUrl</li>
     *   <li>URL构建：将params转换为查询参数拼接到URL</li>
     *   <li>请求头构建：解析headers JSON并设置到HTTP请求</li>
     *   <li>请求体构建：解析body JSON作为请求体</li>
     *   <li>发送HTTP请求：使用RestTemplate发送请求</li>
     *   <li>状态码验证：将实际状态码与expectedStatus比较</li>
     *   <li>断言验证：检查响应体是否包含assertions中的预期内容</li>
     * </ol>
     * </p>
     * <p>
     * 变量替换示例：
     * <ul>
     *   <li>URL: "{{baseUrl}}/api/users" -> "http://localhost:8080/api/users"</li>
     *   <li>Body: "{{token}}" -> "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."</li>
     * </ul>
     * </p>
     * <p>
     * 断言格式（JSON）：
     * <pre>
     * {
     *   "message": "success",
     *   "code": "200"
     * }
     * </pre>
     * 会检查响应体是否包含这些键值对。
     * </p>
     *
     * @param step 测试步骤配置
     * @param environmentId 环境ID，用于变量替换，可以为null
     * @return 步骤执行结果，包含请求/响应详情、状态码匹配结果、断言结果等
     */
    private StepExecutionResult executeStep(TestStep step, Long environmentId) {
        long startTime = System.currentTimeMillis();

        StepExecutionResult result = new StepExecutionResult();
        result.setStepId(step.getId());
        result.setStepName(step.getName());
        result.setStepOrder(step.getStepOrder());
        result.setExpectedStatus(step.getExpectedStatus());

        log.info("========================================");
        log.info("开始执行步骤 [{}]: {}", step.getStepOrder(), step.getName());
        log.info("========================================");

        try {
            // 获取环境变量（如果指定了环境）
            String url = step.getUrl();
            String params = step.getParams();
            String headers = step.getHeaders();
            String body = step.getBody();

            // 替换环境变量
            if (environmentId != null) {
                url = environmentService.replaceVariables(url, environmentId);
                if (params != null) {
                    params = environmentService.replaceVariables(params, environmentId);
                }
                if (headers != null) {
                    headers = environmentService.replaceVariables(headers, environmentId);
                }
                if (body != null) {
                    body = environmentService.replaceVariables(body, environmentId);
                }

                // 如果URL以/开头（相对路径），拼接环境的baseUrl
                if (url != null && url.startsWith("/")) {
                    com.mock.service.entity.Environment environment = environmentService.getEnvironmentById(environmentId);
                    if (environment != null && environment.getBaseUrl() != null && !environment.getBaseUrl().isEmpty()) {
                        url = environment.getBaseUrl() + url;
                    }
                }
            }

            // 构建 URL
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);

            // 添加查询参数
            if (params != null && !params.isEmpty()) {
                Map<String, Object> paramsMap = objectMapper.readValue(params, Map.class);
                paramsMap.forEach((key, value) -> uriBuilder.queryParam(key, value));
            }

            String finalUrl = uriBuilder.toUriString();

            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null && !headers.isEmpty()) {
                Map<String, String> headerMap = objectMapper.readValue(headers, Map.class);
                headerMap.forEach(httpHeaders::add);
            }

            // 设置 Content-Type
            if (body != null && !body.isEmpty()) {
                if (!httpHeaders.containsKey(HttpHeaders.CONTENT_TYPE)) {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                }
            }

            // 构建请求体
            Object requestBody = null;
            if (body != null && !body.isEmpty()) {
                requestBody = objectMapper.readValue(body, Object.class);
            }

            // 构建请求实体
            HttpEntity<Object> entity = new HttpEntity<>(requestBody, httpHeaders);

            // 打印请求信息
            log.info("📤 发送请求:");
            log.info("  ├─ 方法: {}", step.getMethod().toUpperCase());
            log.info("  ├─ URL: {}", finalUrl);
            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                log.info("  ├─ 请求头:");
                httpHeaders.forEach((key, value) -> {
                    log.info("  │   ├─ {}: {}", key, String.join(", ", value));
                });
            }
            if (body != null && !body.isEmpty()) {
                log.info("  └─ 请求体: {}", body.length() > 500 ? body.substring(0, 500) + "..." : body);
            } else {
                log.info("  └─ 请求体: (空)");
            }

            // 发送请求
            HttpMethod method = HttpMethod.valueOf(step.getMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(
                    finalUrl,
                    method,
                    entity,
                    String.class
            );

            // 打印响应信息
            log.info("📥 收到响应:");
            log.info("  ├─ 状态码: {} (预期: {})", response.getStatusCode().value(), step.getExpectedStatus());
            if (response.getHeaders() != null && !response.getHeaders().isEmpty()) {
                log.info("  ├─ 响应头:");
                response.getHeaders().forEach((key, value) -> {
                    log.info("  │   ├─ {}: {}", key, String.join(", ", value));
                });
            }
            String responseBody = response.getBody();
            if (responseBody != null && !responseBody.isEmpty()) {
                log.info("  └─ 响应体: {}", responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
            } else {
                log.info("  └─ 响应体: (空)");
            }

            // 记录请求信息
            result.setRequestUrl(finalUrl);
            result.setRequestMethod(step.getMethod());
            result.setRequestParams(params);
            result.setRequestBody(body);

            // 转换请求头为Map
            Map<String, String> requestHeadersMap = new HashMap<>();
            httpHeaders.forEach((key, value) -> {
                requestHeadersMap.put(key, String.join(", ", value));
            });
            result.setRequestHeaders(requestHeadersMap);

            // 记录响应
            result.setStatusCode(response.getStatusCode().value());
            result.setResponseBody(response.getBody());

            // 转换响应头
            Map<String, String> responseHeaders = new HashMap<>();
            response.getHeaders().forEach((key, value) -> {
                responseHeaders.put(key, String.join(", ", value));
            });
            result.setResponseHeaders(responseHeaders);

            // 验证状态码
            boolean statusMatch = response.getStatusCode().value() == step.getExpectedStatus();

            log.info("🔍 验证结果:");
            if (statusMatch) {
                log.info("  ├─ ✅ 状态码验证: 通过 ({} == {})", response.getStatusCode().value(), step.getExpectedStatus());
            } else {
                log.warn("  ├─ ❌ 状态码验证: 失败 (实际: {}, 预期: {})", response.getStatusCode().value(), step.getExpectedStatus());
            }

            // 执行断言
            boolean assertionPass = true;
            StringBuilder assertionResult = new StringBuilder();

            if (step.getAssertions() != null && !step.getAssertions().isEmpty()) {
                log.info("  ├─ 执行断言:");
                // 简单的断言逻辑：检查响应体是否包含指定内容
                try {
                    Map<String, Object> assertions = objectMapper.readValue(step.getAssertions(), Map.class);
                    // 使用前面已声明的responseBody变量

                    for (Map.Entry<String, Object> assertion : assertions.entrySet()) {
                        String key = assertion.getKey();
                        Object expectedValue = assertion.getValue();

                        if (responseBody != null && responseBody.contains(expectedValue.toString())) {
                            String successMsg = String.format("✓ 断言通过: %s = %s", key, expectedValue);
                            assertionResult.append(successMsg).append("\n");
                            log.info("  │   ├─ ✅ {}", successMsg);
                        } else {
                            assertionPass = false;
                            String failMsg = String.format("✗ 断言失败: %s 应包含 %s", key, expectedValue);
                            assertionResult.append(failMsg).append("\n");
                            log.warn("  │   ├─ ❌ {}", failMsg);
                        }
                    }
                } catch (Exception e) {
                    assertionPass = false;
                    String errorMsg = "断言解析失败: " + e.getMessage();
                    assertionResult.append(errorMsg);
                    log.error("  │   └─ ❌ {}", errorMsg);
                }
            } else {
                log.info("  ├─ 断言: (无断言配置)");
            }

            result.setAssertionResult(assertionResult.toString());
            result.setSuccess(statusMatch && assertionPass);

            if (!statusMatch) {
                result.setErrorMessage(String.format("状态码不匹配: 期望 %d, 实际 %d",
                        step.getExpectedStatus(), response.getStatusCode().value()));
            }

            // 打印步骤执行总结
            long duration = System.currentTimeMillis() - startTime;
            if (result.getSuccess()) {
                log.info("  └─ 结果: ✅ 成功 (耗时: {}ms)", duration);
                log.info("✅ 步骤 [{}] 执行成功", step.getStepOrder());
            } else {
                log.warn("  └─ 结果: ❌ 失败 (耗时: {}ms)", duration);
                log.warn("❌ 步骤 [{}] 执行失败: {}", step.getStepOrder(), result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("❌ 步骤 [{}] 执行异常: {}", step.getStepOrder(), e.getMessage());
            log.error("异常详情:", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            result.setDuration(System.currentTimeMillis() - startTime);
            log.info("========================================");
        }

        return result;
    }
}
