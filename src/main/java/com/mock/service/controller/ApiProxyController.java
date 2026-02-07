package com.mock.service.controller;

import com.mock.service.dto.ProxyRequest;
import com.mock.service.dto.ProxyResponse;
import com.mock.service.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/proxy")
@CrossOrigin(origins = "*")
public class ApiProxyController {

    private final RestTemplate restTemplate;

    public ApiProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public Result<ProxyResponse> proxyRequest(@RequestBody ProxyRequest request) {
        log.info("代理请求: {} {}", request.getMethod(), request.getUrl());

        try {
            // 构建 URL（包含 query 参数）
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(request.getUrl());
            if (request.getParams() != null) {
                request.getParams().forEach(uriBuilder::queryParam);
            }
            String finalUrl = uriBuilder.toUriString();

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(headers::add);
            }

            // 设置 Content-Type
            if (request.getBody() != null) {
                if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                }
            }

            // 构建请求实体
            HttpEntity<Object> entity = new HttpEntity<>(request.getBody(), headers);

            // 发送请求
            HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(
                    finalUrl,
                    method,
                    entity,
                    String.class
            );

            // 构建响应
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setStatus(response.getStatusCode().value());
            proxyResponse.setStatusText(response.getStatusCode().toString());

            // 转换响应头
            Map<String, String> responseHeaders = new HashMap<>();
            response.getHeaders().forEach((key, value) -> {
                responseHeaders.put(key, String.join(", ", value));
            });
            proxyResponse.setHeaders(responseHeaders);

            // 尝试解析 JSON 响应
            String body = response.getBody();
            try {
                // 如果是 JSON，尝试解析
                if (body != null && (body.trim().startsWith("{") || body.trim().startsWith("["))) {
                    proxyResponse.setData(body);
                } else {
                    proxyResponse.setData(body);
                }
            } catch (Exception e) {
                proxyResponse.setData(body);
            }

            log.info("代理请求成功: {} {}", response.getStatusCode(), finalUrl);
            return Result.success(proxyResponse);

        } catch (Exception e) {
            log.error("代理请求失败: {}", e.getMessage(), e);

            // 构建错误响应
            ProxyResponse errorResponse = new ProxyResponse();
            errorResponse.setStatus(500);
            errorResponse.setStatusText("Internal Server Error");
            errorResponse.setHeaders(new HashMap<>());
            errorResponse.setData(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));

            return Result.success(errorResponse);
        }
    }
}
