package com.mock.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * GET 请求参数解析和导出工具类
 */
public class GetParamExporter {

    /**
     * 从完整 URL 中解析参数
     * @param url 完整的 URL（包含参数）
     * @return 参数 Map
     */
    public static Map<String, String> parseUrlParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();

        if (url == null || !url.contains("?")) {
            return params;
        }

        // 提取查询字符串部分
        String queryString = url.substring(url.indexOf("?") + 1);

        // 分割参数
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);

                // URL 解码
                try {
                    key = URLDecoder.decode(key, StandardCharsets.UTF_8.name());
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    // UTF-8 should always be supported
                }

                params.put(key, value);
            }
        }

        return params;
    }

    /**
     * 将参数 Map 导出为 JSON 字符串
     */
    public static String exportToJson(Map<String, String> params) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 将参数 Map 导出为表格形式（Markdown）
     */
    public static String exportToMarkdownTable(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("| 参数名 | 参数值 |\n");
        sb.append("|--------|--------|\n");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("| ").append(entry.getKey())
              .append(" | ").append(entry.getValue())
              .append(" |\n");
        }

        return sb.toString();
    }

    /**
     * 将参数 Map 导出为键值对文本
     */
    public static String exportToKeyValueText(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 将参数 Map 导出为 cURL 命令
     */
    public static String exportToCurl(String baseUrl, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("curl -X GET \"").append(baseUrl);

        if (!params.isEmpty()) {
            sb.append("?");
            List<String> paramPairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramPairs.add(entry.getKey() + "=" + entry.getValue());
            }
            sb.append(String.join("&", paramPairs));
        }

        sb.append("\"");
        return sb.toString();
    }

    /**
     * 将参数 Map 导出为 Postman Collection 格式的 JSON
     */
    public static String exportToPostmanFormat(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"url\": \"").append(url.split("\\?")[0]).append("\",\n");
        sb.append("  \"method\": \"GET\",\n");
        sb.append("  \"params\": [\n");

        int index = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("    {\n");
            sb.append("      \"key\": \"").append(entry.getKey()).append("\",\n");
            sb.append("      \"value\": \"").append(entry.getValue()).append("\",\n");
            sb.append("      \"enabled\": true\n");
            sb.append("    }");
            if (++index < params.size()) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 导出为 Java Map 初始化代码
     */
    public static String exportToJavaCode(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("Map<String, String> params = new HashMap<>();\n");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("params.put(\"").append(entry.getKey())
              .append("\", \"").append(entry.getValue()).append("\");\n");
        }
        return sb.toString();
    }

    /**
     * 主函数 - 演示各种导出方式
     */
    public static void main(String[] args) {
        // 测试 URL（OCPX 点击上报示例）
        String testUrl = "https://advertiser.example.com/report?" +
                "reportType=6" +
                "&requestId=test-123456" +
                "&os=1" +
                "&imei=862123456789012" +
                "&imeiMd5=071df1152cda3260b02fcc9e1d0f8b2a" +
                "&callbackUrl=https%3A%2F%2Fcallback.yoyo.com%2Focpx%2Ftransform" +
                "&ip=192.168.1.100" +
                "&ua=Dalvik%2F2.1.0+%28Linux%3B+U%3B+Android+15%3B+AGT-AN00+Build%2FHONORAGT-AN00%29++com.hihonor.hos%2F10.0.4.303" +
                "&os_ver=15" +
                "&brand=HONOR" +
                "&model=AGT-AN00";

        System.out.println("========== GET 请求参数导出工具 ==========\n");

        // 1. 解析参数
        System.out.println("【1】原始 URL");
        System.out.println("─".repeat(80));
        System.out.println(testUrl);
        System.out.println("\n" + "=".repeat(80) + "\n");

        Map<String, String> params = parseUrlParams(testUrl);

        // 2. 导出为 JSON
        System.out.println("【2】导出为 JSON 格式");
        System.out.println("─".repeat(80));
        System.out.println(exportToJson(params));
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 3. 导出为 Markdown 表格
        System.out.println("【3】导出为 Markdown 表格");
        System.out.println("─".repeat(80));
        System.out.println(exportToMarkdownTable(params));
        System.out.println("=".repeat(80) + "\n");

        // 4. 导出为键值对文本
        System.out.println("【4】导出为键值对文本");
        System.out.println("─".repeat(80));
        System.out.println(exportToKeyValueText(params));
        System.out.println("=".repeat(80) + "\n");

        // 5. 导出为 cURL 命令
        System.out.println("【5】导出为 cURL 命令");
        System.out.println("─".repeat(80));
        System.out.println(exportToCurl("https://advertiser.example.com/report", params));
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 6. 导出为 Postman 格式
        System.out.println("【6】导出为 Postman 格式");
        System.out.println("─".repeat(80));
        System.out.println(exportToPostmanFormat(testUrl, params));
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 7. 导出为 Java 代码
        System.out.println("【7】导出为 Java 代码");
        System.out.println("─".repeat(80));
        System.out.println(exportToJavaCode(params));
        System.out.println("=".repeat(80) + "\n");

        // 8. 显示参数统计
        System.out.println("【8】参数统计");
        System.out.println("─".repeat(80));
        System.out.println("总参数数量: " + params.size());
        System.out.println("参数列表: " + String.join(", ", params.keySet()));
        System.out.println("\n" + "=".repeat(80));
    }
}
