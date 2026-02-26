package com.mock.service.controller;

import com.mock.service.entity.MockConfig;
import com.mock.service.service.MockConfigService;
import com.mock.service.service.MockHistoryService;
import com.mock.service.service.ProtoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * 动态Mock接口控制器
 * <p>
 * 这是Mock服务的核心控制器，负责处理所有 /api/mock/** 路径的请求。
 * 根据请求的路径和HTTP方法动态匹配对应的Mock配置，返回模拟响应。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>支持所有HTTP方法（GET/POST/PUT/DELETE/PATCH/OPTIONS/HEAD）</li>
 *   <li>动态路径匹配：根据请求路径和方法查找启用的Mock配置</li>
 *   <li>响应延迟模拟：支持配置延迟时间（毫秒）</li>
 *   <li>自定义响应状态码和Content-Type</li>
 *   <li>支持 JSON 和 Protobuf 两种响应格式</li>
 *   <li>请求历史记录：异步保存每次调用的详细信息</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // Mock配置: path="/users", method="GET", statusCode=200
 * GET http://localhost:8080/api/mock/users
 * -> 返回Mock配置的responseBody
 *
 * // Mock配置: path="/users", method="POST", statusCode=201, delay=1000
 * POST http://localhost:8080/api/mock/users
 * -> 延迟1秒后返回responseBody
 *
 * // Mock配置: path="/proto/data", responseType="PROTOBUF"
 * GET http://localhost:8080/api/mock/proto/data
 * -> 返回protobuf二进制数据
 * </pre>
 * </p>
 *
 * @author Mock Service Team
 * @version 1.0
 * @since 2024-01-01
 * @see MockConfigService
 * @see MockHistoryService
 * @see ProtoService
 */
@Slf4j
@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DynamicMockController {

    /** Mock配置服务，用于查找匹配的Mock配置 */
    private final MockConfigService mockConfigService;

    /** Mock历史服务，用于保存调用记录 */
    private final MockHistoryService mockHistoryService;

    /** Proto服务，用于处理Protobuf格式响应 */
    private final ProtoService protoService;

    /**
     * 处理所有Mock请求
     * <p>
     * 这是核心的Mock处理方法，执行流程：
     * <ol>
     *   <li>解析请求路径和HTTP方法</li>
     *   <li>查找匹配的Mock配置（必须是启用状态）</li>
     *   <li>读取请求体内容</li>
     *   <li>应用配置的响应延迟</li>
     *   <li>根据responseType构建响应（JSON或Protobuf）</li>
     *   <li>异步保存调用历史记录</li>
     *   <li>返回模拟响应</li>
     * </ol>
     * </p>
     *
     * @param request HttpServletRequest对象，包含请求的所有信息
     * @return ResponseEntity 包含Mock配置的响应体、状态码和Content-Type
     */
    @RequestMapping(value = "/**", method = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.PATCH,
        RequestMethod.OPTIONS,
        RequestMethod.HEAD
    })
    public ResponseEntity<?> handleMockRequest(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 获取请求路径（去掉 /api/mock 前缀）
            String requestPath = request.getRequestURI().replace("/api/mock", "");
            String method = request.getMethod();

            log.info("收到 Mock 请求: {} {}", method, requestPath);

            // 查找匹配的 Mock 配置
            MockConfig mockConfig = mockConfigService.findMockByPathAndMethod(requestPath, method);

            if (mockConfig == null) {
                log.warn("未找到匹配的 Mock 配置: {} {}", method, requestPath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"未找到匹配的 Mock 配置\"}");
            }

            // 读取请求体
            String requestBody = null;
            try {
                requestBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("读取请求体失败", e);
            }

            // 模拟延迟
            if (mockConfig.getDelay() != null && mockConfig.getDelay() > 0) {
                Thread.sleep(mockConfig.getDelay());
            }

            // 准备响应
            String responseBody = mockConfig.getResponseBody();
            Integer statusCode = mockConfig.getStatusCode();
            String contentType = mockConfig.getContentType();

            // 计算响应时间
            long responseTime = System.currentTimeMillis() - startTime;

            // 保存历史记录（异步，记录原始 JSON 格式的 responseBody）
            try {
                mockHistoryService.saveHistory(
                    mockConfig.getId(),
                    mockConfig.getName(),
                    request,
                    requestBody,
                    statusCode,
                    responseBody,
                    responseTime
                );
            } catch (Exception e) {
                log.error("保存历史记录失败", e);
            }

            // 根据响应类型返回不同格式
            if ("PROTOBUF".equals(mockConfig.getResponseType())) {
                return handleProtobufResponse(mockConfig, statusCode, responseBody);
            }

            // 默认 JSON/文本响应
            return ResponseEntity
                .status(statusCode)
                .contentType(MediaType.parseMediaType(contentType))
                .body(responseBody);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Mock 请求处理被中断", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"请求处理被中断\"}");
        } catch (Exception e) {
            log.error("处理 Mock 请求失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 处理 Protobuf 格式响应
     * <p>
     * 将 MockConfig 中的 JSON 格式 responseBody 转换为 Protobuf 二进制数据返回。
     * </p>
     */
    private ResponseEntity<?> handleProtobufResponse(MockConfig mockConfig, Integer statusCode, String responseBody) {
        try {
            if (mockConfig.getProtoFileId() == null || mockConfig.getProtoMessageType() == null) {
                log.error("Protobuf 响应配置不完整: protoFileId={}, protoMessageType={}",
                    mockConfig.getProtoFileId(), mockConfig.getProtoMessageType());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Protobuf 响应配置不完整，缺少 protoFileId 或 protoMessageType\"}");
            }

            byte[] protoBytes = protoService.jsonToProtobuf(
                mockConfig.getProtoFileId(),
                mockConfig.getProtoMessageType(),
                responseBody
            );

            log.info("Protobuf 响应生成成功: messageType={}, 大小={} bytes",
                mockConfig.getProtoMessageType(), protoBytes.length);

            return ResponseEntity
                .status(statusCode)
                .contentType(MediaType.parseMediaType("application/x-protobuf"))
                .body(protoBytes);

        } catch (Exception e) {
            log.error("生成 Protobuf 响应失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Protobuf 响应生成失败: " + e.getMessage() + "\"}");
        }
    }
}
