package com.mock.service.controller;

import com.mock.service.dto.Result;
import com.mock.service.util.GetParamExporter;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * GET 请求参数示例 Controller
 * 演示如何在 Spring Boot 中接收和导出 GET 参数
 */
@RestController
@RequestMapping("/api/params")
public class GetParamExampleController {

    /**
     * 方式1: 使用 @RequestParam 接收单个参数
     */
    @GetMapping("/method1")
    public Result<Map<String, String>> receiveParamsByRequestParam(
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false) String os,
            @RequestParam(required = false) String imei,
            @RequestParam(required = false) String ua) {

        Map<String, String> params = new LinkedHashMap<>();
        params.put("reportType", reportType);
        params.put("requestId", requestId);
        params.put("os", os);
        params.put("imei", imei);
        params.put("ua", ua);

        return Result.success(params);
    }

    /**
     * 方式2: 使用 @RequestParam Map 接收所有参数
     */
    @GetMapping("/method2")
    public Result<Map<String, Object>> receiveAllParams(
            @RequestParam Map<String, String> allParams) {

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("params", allParams);
        result.put("count", allParams.size());
        result.put("keys", allParams.keySet());

        return Result.success(result);
    }

    /**
     * 方式3: 使用 HttpServletRequest 获取所有参数
     */
    @GetMapping("/method3")
    public Result<Map<String, Object>> receiveParamsByRequest(HttpServletRequest request) {

        Map<String, String> params = new LinkedHashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("params", params);
        result.put("queryString", request.getQueryString());
        result.put("fullUrl", request.getRequestURL() + "?" + request.getQueryString());

        return Result.success(result);
    }

    /**
     * 方式4: 导出参数为 JSON 格式
     */
    @GetMapping("/export/json")
    public Result<String> exportParamsAsJson(@RequestParam Map<String, String> allParams) {
        String json = GetParamExporter.exportToJson(allParams);
        return Result.success(json);
    }

    /**
     * 方式5: 导出参数为 Markdown 表格
     */
    @GetMapping("/export/table")
    public Result<String> exportParamsAsTable(@RequestParam Map<String, String> allParams) {
        String table = GetParamExporter.exportToMarkdownTable(allParams);
        return Result.success(table);
    }

    /**
     * 方式6: 导出参数为 cURL 命令
     */
    @GetMapping("/export/curl")
    public Result<String> exportParamsAsCurl(
            HttpServletRequest request,
            @RequestParam Map<String, String> allParams) {

        String baseUrl = request.getRequestURL().toString();
        String curl = GetParamExporter.exportToCurl(baseUrl, allParams);
        return Result.success(curl);
    }

    /**
     * 方式7: 解析并导出外部 URL 的参数
     */
    @PostMapping("/parse")
    public Result<Map<String, Object>> parseExternalUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");

        if (url == null || url.isEmpty()) {
            return Result.error("URL 不能为空");
        }

        // 解析 URL 参数
        Map<String, String> params = GetParamExporter.parseUrlParams(url);

        // 导出为多种格式
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalUrl", url);
        result.put("params", params);
        result.put("paramsCount", params.size());
        result.put("jsonFormat", GetParamExporter.exportToJson(params));
        result.put("tableFormat", GetParamExporter.exportToMarkdownTable(params));
        result.put("curlFormat", GetParamExporter.exportToCurl(url.split("\\?")[0], params));

        return Result.success(result);
    }

    /**
     * OCPX 专用: 接收点击上报参数并导出
     */
    @GetMapping("/ocpx/click")
    public Result<Map<String, Object>> receiveOcpxClickReport(
            @RequestParam String reportType,
            @RequestParam String requestId,
            @RequestParam String os,
            @RequestParam(required = false) String imei,
            @RequestParam(required = false) String imeiMd5,
            @RequestParam(required = false) String oaid,
            @RequestParam(required = false) String oaidMd5,
            @RequestParam(required = false) String idfa,
            @RequestParam(required = false) String idfaMd5,
            @RequestParam String callbackUrl,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String ua,
            @RequestParam String os_ver,
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Map<String, String> allParams) {

        Map<String, Object> result = new LinkedHashMap<>();

        // 基本信息
        result.put("reportType", reportType);
        result.put("reportTypeName", "6".equals(reportType) ? "点击" : "曝光");
        result.put("os", os);
        result.put("osName", "1".equals(os) ? "Android" : "iOS");
        result.put("brand", brand);
        result.put("model", model);

        // 所有参数
        result.put("allParams", allParams);

        // 导出格式
        result.put("jsonExport", GetParamExporter.exportToJson(allParams));
        result.put("tableExport", GetParamExporter.exportToMarkdownTable(allParams));

        // 模拟返回成功响应（根据文档要求）
        result.put("code", 0);
        result.put("message", "上报成功");

        return Result.success(result);
    }

    /**
     * 测试接口: 打印接收到的所有参数
     */
    @GetMapping("/debug")
    public Result<Map<String, Object>> debugParams(
            HttpServletRequest request,
            @RequestParam Map<String, String> allParams) {

        Map<String, Object> debugInfo = new LinkedHashMap<>();

        // 请求信息
        debugInfo.put("requestMethod", request.getMethod());
        debugInfo.put("requestUrl", request.getRequestURL().toString());
        debugInfo.put("queryString", request.getQueryString());

        // 参数详情
        debugInfo.put("paramCount", allParams.size());
        debugInfo.put("params", allParams);

        // Headers
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        debugInfo.put("headers", headers);

        // 各种导出格式
        debugInfo.put("exportFormats", Map.of(
                "json", GetParamExporter.exportToJson(allParams),
                "table", GetParamExporter.exportToMarkdownTable(allParams),
                "keyValue", GetParamExporter.exportToKeyValueText(allParams),
                "javaCode", GetParamExporter.exportToJavaCode(allParams)
        ));

        return Result.success(debugInfo);
    }
}
