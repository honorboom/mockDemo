package com.mock.service.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

/**
 * Apifox 数据导入工具
 * 支持从 Apifox 导出的 Postman Collection 格式中提取参数
 */
public class ApifoxImporter {

    /**
     * 从 Postman Collection JSON 文件中导入所有接口参数
     * @param jsonFilePath JSON 文件路径
     * @return 所有接口的参数列表
     */
    public static List<ApiInfo> importFromPostmanCollection(String jsonFilePath) {
        List<ApiInfo> apiList = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(jsonFilePath));

            // 遍历所有接口
            JsonNode items = root.get("item");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    ApiInfo apiInfo = parseApiItem(item);
                    if (apiInfo != null) {
                        apiList.add(apiInfo);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("导入失败: " + e.getMessage());
            e.printStackTrace();
        }

        return apiList;
    }

    /**
     * 解析单个接口信息
     */
    private static ApiInfo parseApiItem(JsonNode item) {
        ApiInfo apiInfo = new ApiInfo();

        // 接口名称
        if (item.has("name")) {
            apiInfo.name = item.get("name").asText();
        }

        JsonNode request = item.get("request");
        if (request == null) {
            return null;
        }

        // 请求方法
        if (request.has("method")) {
            apiInfo.method = request.get("method").asText();
        }

        // URL 信息
        JsonNode url = request.get("url");
        if (url != null) {
            // 完整 URL
            if (url.has("raw")) {
                apiInfo.fullUrl = url.get("raw").asText();
            }

            // Query 参数
            JsonNode query = url.get("query");
            if (query != null && query.isArray()) {
                for (JsonNode param : query) {
                    String key = param.get("key").asText();
                    String value = param.has("value") ? param.get("value").asText() : "";
                    String description = param.has("description") ? param.get("description").asText() : "";
                    boolean disabled = param.has("disabled") && param.get("disabled").asBoolean();

                    if (!disabled) {
                        apiInfo.queryParams.put(key, value);
                        if (!description.isEmpty()) {
                            apiInfo.paramDescriptions.put(key, description);
                        }
                    }
                }
            }

            // 路径
            JsonNode path = url.get("path");
            if (path != null && path.isArray()) {
                StringBuilder pathBuilder = new StringBuilder();
                for (JsonNode segment : path) {
                    pathBuilder.append("/").append(segment.asText());
                }
                apiInfo.path = pathBuilder.toString();
            }
        }

        // Headers
        JsonNode headers = request.get("header");
        if (headers != null && headers.isArray()) {
            for (JsonNode header : headers) {
                String key = header.get("key").asText();
                String value = header.has("value") ? header.get("value").asText() : "";
                apiInfo.headers.put(key, value);
            }
        }

        return apiInfo;
    }

    /**
     * 从 Apifox 复制的完整 URL 中快速提取参数
     * @param apifoxUrl 从 Apifox 复制的完整 URL
     * @return 参数 Map
     */
    public static Map<String, String> quickParseFromUrl(String apifoxUrl) {
        return GetParamExporter.parseUrlParams(apifoxUrl);
    }

    /**
     * 接口信息类
     */
    public static class ApiInfo {
        public String name;
        public String method;
        public String fullUrl;
        public String path;
        public Map<String, String> queryParams = new LinkedHashMap<>();
        public Map<String, String> paramDescriptions = new LinkedHashMap<>();
        public Map<String, String> headers = new LinkedHashMap<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("接口名称: ").append(name).append("\n");
            sb.append("请求方法: ").append(method).append("\n");
            sb.append("路径: ").append(path).append("\n");
            sb.append("完整URL: ").append(fullUrl).append("\n");
            sb.append("Query参数数量: ").append(queryParams.size()).append("\n");
            sb.append("参数详情:\n");
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue());
                if (paramDescriptions.containsKey(entry.getKey())) {
                    sb.append(" (").append(paramDescriptions.get(entry.getKey())).append(")");
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        /**
         * 导出为我们工具的 Map 格式
         */
        public Map<String, String> toParamMap() {
            return new LinkedHashMap<>(queryParams);
        }
    }

    /**
     * 演示：从 Apifox URL 快速导入并导出为多种格式
     */
    public static void demonstrateQuickImport() {
        System.out.println("========== Apifox 快速导入演示 ==========\n");

        // 模拟从 Apifox 复制的 URL
        String apifoxUrl = "https://advertiser.example.com/report?" +
                "reportType=6&" +
                "requestId=test-123456&" +
                "os=1&" +
                "imei=862123456789012&" +
                "imeiMd5=071df1152cda3260b02fcc9e1d0f8b2a&" +
                "callbackUrl=https%3A%2F%2Fcallback.yoyo.com%2Focpx%2Ftransform&" +
                "ip=192.168.1.100&" +
                "ua=Dalvik%2F2.1.0+%28Linux%3B+U%3B+Android+15%3B+AGT-AN00+Build%2FHONORAGT-AN00%29++com.hihonor.hos%2F10.0.4.303&" +
                "os_ver=15&" +
                "brand=HONOR&" +
                "model=AGT-AN00";

        System.out.println("【步骤1】从 Apifox 复制的 URL:");
        System.out.println(apifoxUrl);
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 解析参数
        Map<String, String> params = quickParseFromUrl(apifoxUrl);

        System.out.println("【步骤2】解析出的参数（已自动 URLDecode）:");
        System.out.println("参数数量: " + params.size());
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 导出为 JSON
        System.out.println("【步骤3】导出为 JSON 格式:");
        System.out.println(GetParamExporter.exportToJson(params));
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 导出为表格
        System.out.println("【步骤4】导出为 Markdown 表格:");
        System.out.println(GetParamExporter.exportToMarkdownTable(params));
        System.out.println("=".repeat(80) + "\n");

        // 导出为 cURL
        System.out.println("【步骤5】导出为 cURL 命令:");
        System.out.println(GetParamExporter.exportToCurl(
                "https://advertiser.example.com/report", params));
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 导出为 Java 代码
        System.out.println("【步骤6】导出为 Java 代码:");
        System.out.println(GetParamExporter.exportToJavaCode(params));
        System.out.println("=".repeat(80) + "\n");

        System.out.println("✅ 导入完成！您可以直接使用这些数据。");
    }

    /**
     * 演示：从 Postman Collection JSON 导入
     */
    public static void demonstrateJsonImport(String jsonFilePath) {
        System.out.println("========== 从 Postman Collection 导入 ==========\n");

        List<ApiInfo> apiList = importFromPostmanCollection(jsonFilePath);

        System.out.println("成功导入 " + apiList.size() + " 个接口\n");
        System.out.println("=".repeat(80) + "\n");

        for (int i = 0; i < apiList.size(); i++) {
            ApiInfo api = apiList.get(i);
            System.out.println("接口 " + (i + 1) + ":");
            System.out.println(api);
            System.out.println("JSON 格式:");
            System.out.println(GetParamExporter.exportToJson(api.toParamMap()));
            System.out.println("\n" + "=".repeat(80) + "\n");
        }
    }

    /**
     * 主函数
     */
    public static void main(String[] args) {
        // 演示1: 从 URL 快速导入（最常用）
        demonstrateQuickImport();

        System.out.println("\n\n");

        // 演示2: 从 JSON 文件导入（如果有导出的 JSON 文件）
        // 取消下面的注释并提供正确的文件路径
        // String jsonFile = "D:/apifox-export/collection.json";
        // if (new File(jsonFile).exists()) {
        //     demonstrateJsonImport(jsonFile);
        // } else {
        //     System.out.println("提示: 如需从 JSON 文件导入，请在 Apifox 中导出为 Postman Collection");
        //     System.out.println("然后将文件路径传入 demonstrateJsonImport() 方法");
        // }

        System.out.println("\n使用提示:");
        System.out.println("1. 在 Apifox 中复制完整 URL");
        System.out.println("2. 调用 ApifoxImporter.quickParseFromUrl(url)");
        System.out.println("3. 使用 GetParamExporter 导出为任意格式");
    }
}
