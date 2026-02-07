package com.mock.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mock.service.entity.MockHistory;
import com.mock.service.repository.MockHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock 历史记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockHistoryService {

    private final MockHistoryRepository mockHistoryRepository;
    private final ObjectMapper objectMapper;

    /**
     * 保存 Mock 调用历史
     */
    @Transactional
    public MockHistory saveHistory(
        Long mockConfigId,
        String mockName,
        HttpServletRequest request,
        String requestBody,
        Integer responseStatus,
        String responseBody,
        Long responseTime
    ) {
        MockHistory history = new MockHistory();
        history.setMockConfigId(mockConfigId);
        history.setMockName(mockName);
        history.setRequestPath(request.getRequestURI());
        history.setRequestMethod(request.getMethod());

        // 保存请求参数
        history.setRequestParams(getRequestParams(request));

        // 保存请求头
        history.setRequestHeaders(getRequestHeaders(request));

        // 保存请求体
        history.setRequestBody(requestBody);

        history.setResponseStatus(responseStatus);
        history.setResponseBody(responseBody);
        history.setResponseTime(responseTime);
        history.setClientIp(getClientIp(request));

        return mockHistoryRepository.save(history);
    }

    /**
     * 分页查询历史记录
     */
    public Page<MockHistory> getHistoryPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return mockHistoryRepository.findAll(pageable);
    }

    /**
     * 根据 Mock 配置 ID 分页查询历史记录
     */
    public Page<MockHistory> getHistoryByMockId(Long mockConfigId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return mockHistoryRepository.findByMockConfigIdOrderByCreatedAtDesc(mockConfigId, pageable);
    }

    /**
     * 根据时间范围查询历史记录
     */
    public Page<MockHistory> getHistoryByTimeRange(
        LocalDateTime startTime,
        LocalDateTime endTime,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return mockHistoryRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
            startTime, endTime, pageable
        );
    }

    /**
     * 获取最近的历史记录
     */
    public List<MockHistory> getRecentHistory() {
        return mockHistoryRepository.findTop100ByOrderByCreatedAtDesc();
    }

    /**
     * 统计 Mock 调用次数
     */
    public long countByMockId(Long mockConfigId) {
        return mockHistoryRepository.countByMockConfigId(mockConfigId);
    }

    /**
     * 删除所有历史记录
     */
    @Transactional
    public void deleteAllHistory() {
        log.info("开始删除所有历史记录");
        long count = mockHistoryRepository.count();
        mockHistoryRepository.deleteAll();
        mockHistoryRepository.flush();
        log.info("成功删除 {} 条历史记录", count);
    }

    /**
     * 删除指定 Mock 的历史记录
     */
    @Transactional
    public void deleteHistoryByMockId(Long mockConfigId) {
        log.info("开始删除 Mock ID={} 的历史记录", mockConfigId);
        long count = mockHistoryRepository.countByMockConfigId(mockConfigId);
        mockHistoryRepository.deleteByMockConfigId(mockConfigId);
        mockHistoryRepository.flush();
        log.info("成功删除 {} 条历史记录", count);
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(HttpServletRequest request) {
        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            if (paramMap.isEmpty()) {
                return null;
            }
            return objectMapper.writeValueAsString(paramMap);
        } catch (Exception e) {
            log.error("解析请求参数失败", e);
            return null;
        }
    }

    /**
     * 获取请求头
     */
    private String getRequestHeaders(HttpServletRequest request) {
        try {
            Map<String, String> headerMap = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headerMap.put(headerName, request.getHeader(headerName));
            }
            return objectMapper.writeValueAsString(headerMap);
        } catch (Exception e) {
            log.error("解析请求头失败", e);
            return null;
        }
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
