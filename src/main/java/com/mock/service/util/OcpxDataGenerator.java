package com.mock.service.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * 优优广告主 OCPX 接口测试数据生成器
 * 根据接口文档生成点击上报和转化回传的测试数据
 */
public class OcpxDataGenerator {

    /**
     * 点击上报数据模型
     */
    public static class ClickReportData {
        public String reportType;      // 上报类型：5=曝光, 6=点击
        public String requestId;       // 请求ID
        public String os;              // 1=安卓, 2=iOS
        public String imei;            // 设备IMEI（Android）
        public String imeiMd5;         // IMEI的MD5
        public String oaid;            // OAID（Android）
        public String oaidMd5;         // OAID的MD5
        public String idfa;            // IDFA（iOS）
        public String idfaMd5;         // IDFA的MD5
        public String callbackUrl;     // 回调地址
        public String ip;              // IP地址
        public String ua;              // User-Agent
        public String os_ver;          // 操作系统版本
        public String brand;           // 设备品牌
        public String model;           // 设备型号

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("reportType=").append(reportType);
            sb.append("&requestId=").append(requestId);
            sb.append("&os=").append(os);
            if (imei != null) sb.append("&imei=").append(imei);
            if (imeiMd5 != null) sb.append("&imeiMd5=").append(imeiMd5);
            if (oaid != null) sb.append("&oaid=").append(oaid);
            if (oaidMd5 != null) sb.append("&oaidMd5=").append(oaidMd5);
            if (idfa != null) sb.append("&idfa=").append(idfa);
            if (idfaMd5 != null) sb.append("&idfaMd5=").append(idfaMd5);
            sb.append("&callbackUrl=").append(urlEncode(callbackUrl));
            if (ip != null) sb.append("&ip=").append(ip);
            if (ua != null) sb.append("&ua=").append(urlEncode(ua));
            sb.append("&os_ver=").append(urlEncode(os_ver));
            sb.append("&brand=").append(brand);
            sb.append("&model=").append(urlEncode(model));
            return sb.toString();
        }
    }

    /**
     * 生成MD5
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * URL编码
     */
    public static String urlEncode(String input) {
        if (input == null) return "";
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    /**
     * 生成Android点击上报测试数据
     */
    public static ClickReportData generateAndroidClickData() {
        ClickReportData data = new ClickReportData();

        // 基础必填参数
        data.reportType = "6";  // 6=点击
        data.requestId = UUID.randomUUID().toString();
        data.os = "1";  // 1=安卓

        // Android设备信息
        String imeiRaw = "862123456789012";
        String oaidRaw = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

        data.imei = imeiRaw;
        data.imeiMd5 = md5(imeiRaw);
        data.oaid = oaidRaw;
        data.oaidMd5 = md5(oaidRaw);

        // 回调地址
        data.callbackUrl = "https://callback.yoyo.com/ocpx/transform";

        // 其他信息
        data.ip = "192.168.1.100";
        data.ua = "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";
        data.os_ver = "13";
        data.brand = "Samsung";
        data.model = "SM-G991B";

        return data;
    }

    /**
     * 生成iOS点击上报测试数据
     */
    public static ClickReportData generateiOSClickData() {
        ClickReportData data = new ClickReportData();

        // 基础必填参数
        data.reportType = "6";  // 6=点击
        data.requestId = UUID.randomUUID().toString();
        data.os = "2";  // 2=iOS

        // iOS设备信息
        String idfaRaw = "12345678-1234-1234-1234-123456789012";

        data.idfa = idfaRaw;
        data.idfaMd5 = md5(idfaRaw);

        // 回调地址
        data.callbackUrl = "https://callback.yoyo.com/ocpx/transform";

        // 其他信息
        data.ip = "192.168.1.101";
        data.ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";
        data.os_ver = "17.0";
        data.brand = "Apple";
        data.model = "iPhone 15 Pro";

        return data;
    }

    /**
     * 生成Android曝光上报测试数据
     */
    public static ClickReportData generateAndroidImpressionData() {
        ClickReportData data = generateAndroidClickData();
        data.reportType = "5";  // 5=曝光
        data.requestId = UUID.randomUUID().toString();
        return data;
    }

    /**
     * 生成iOS曝光上报测试数据
     */
    public static ClickReportData generateiOSImpressionData() {
        ClickReportData data = generateiOSClickData();
        data.reportType = "5";  // 5=曝光
        data.requestId = UUID.randomUUID().toString();
        return data;
    }

    /**
     * 生成转化回传URL
     * @param callbackUrl 回调地址（需要先decode）
     * @param transformType 转化类型：1=激活, 2=新登, 3=唤醒, 4=次日回访, 5=下单, 6=购买, 7=首唤, 8=首购
     */
    public static String generateTransformCallbackUrl(String callbackUrl, int transformType) {
        // 先decode回调地址，然后拼接transformType参数
        String decodedUrl = java.net.URLDecoder.decode(callbackUrl, StandardCharsets.UTF_8);

        // 判断URL是否已有参数
        String separator = decodedUrl.contains("?") ? "&" : "?";

        return decodedUrl + separator + "transformType=" + transformType;
    }

    /**
     * 主函数 - 生成各种测试数据
     */
    public static void main(String[] args) {
        System.out.println("========== 优优广告主 OCPX 接口测试数据 ==========\n");

        // 1. Android点击上报
        System.out.println("【1】Android 点击上报完整URL");
        System.out.println("─".repeat(80));
        ClickReportData androidClick = generateAndroidClickData();
        String advertiserUrl = "https://advertiser.example.com/report";
        String fullAndroidClickUrl = advertiserUrl + "?" + androidClick.toString();
        System.out.println(fullAndroidClickUrl);
        System.out.println("\n详细参数:");
        printDetailedParams(androidClick);
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 2. iOS点击上报
        System.out.println("【2】iOS 点击上报完整URL");
        System.out.println("─".repeat(80));
        ClickReportData iOSClick = generateiOSClickData();
        String fulliOSClickUrl = advertiserUrl + "?" + iOSClick.toString();
        System.out.println(fulliOSClickUrl);
        System.out.println("\n详细参数:");
        printDetailedParams(iOSClick);
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 3. Android曝光上报
        System.out.println("【3】Android 曝光上报完整URL");
        System.out.println("─".repeat(80));
        ClickReportData androidImp = generateAndroidImpressionData();
        String fullAndroidImpUrl = advertiserUrl + "?" + androidImp.toString();
        System.out.println(fullAndroidImpUrl);
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 4. iOS曝光上报
        System.out.println("【4】iOS 曝光上报完整URL");
        System.out.println("─".repeat(80));
        ClickReportData iOSImp = generateiOSImpressionData();
        String fulliOSImpUrl = advertiserUrl + "?" + iOSImp.toString();
        System.out.println(fulliOSImpUrl);
        System.out.println("\n" + "=".repeat(80) + "\n");

        // 5. 转化事件回传示例
        System.out.println("【5】转化事件回传URL示例");
        System.out.println("─".repeat(80));
        String[] transformTypes = {
            "1 - 激活", "2 - 新登", "3 - 唤醒", "4 - 次日回访",
            "5 - 下单", "6 - 购买", "7 - 首唤", "8 - 首购"
        };

        for (String typeDesc : transformTypes) {
            String[] parts = typeDesc.split(" - ");
            int type = Integer.parseInt(parts[0]);
            String desc = parts[1];
            String transformUrl = generateTransformCallbackUrl(androidClick.callbackUrl, type);
            System.out.println(desc + " (transformType=" + type + "):");
            System.out.println(transformUrl);
            System.out.println();
        }

        System.out.println("=".repeat(80) + "\n");

        // 6. 多设备品牌示例
        System.out.println("【6】多种Android设备品牌示例");
        System.out.println("─".repeat(80));
        generateMultiDeviceExamples();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试数据生成完成！");
    }

    /**
     * 打印详细参数
     */
    private static void printDetailedParams(ClickReportData data) {
        System.out.println("  reportType: " + data.reportType + " (" + (data.reportType.equals("5") ? "曝光" : "点击") + ")");
        System.out.println("  requestId: " + data.requestId);
        System.out.println("  os: " + data.os + " (" + (data.os.equals("1") ? "Android" : "iOS") + ")");
        if (data.imei != null) System.out.println("  imei: " + data.imei);
        if (data.imeiMd5 != null) System.out.println("  imeiMd5: " + data.imeiMd5);
        if (data.oaid != null) System.out.println("  oaid: " + data.oaid);
        if (data.oaidMd5 != null) System.out.println("  oaidMd5: " + data.oaidMd5);
        if (data.idfa != null) System.out.println("  idfa: " + data.idfa);
        if (data.idfaMd5 != null) System.out.println("  idfaMd5: " + data.idfaMd5);
        System.out.println("  callbackUrl: " + data.callbackUrl + " (编码后)");
        System.out.println("  ip: " + data.ip);
        System.out.println("  ua: " + data.ua + " (编码后)");
        System.out.println("  os_ver: " + data.os_ver);
        System.out.println("  brand: " + data.brand);
        System.out.println("  model: " + data.model);
    }

    /**
     * 生成多设备品牌示例
     */
    private static void generateMultiDeviceExamples() {
        String[][] devices = {
            {"Huawei", "Mate 60 Pro", "14", "Mozilla/5.0 (Linux; Android 14; NOH-AN00) AppleWebKit/537.36"},
            {"Xiaomi", "Xiaomi 14", "14", "Mozilla/5.0 (Linux; Android 14; 23127PN0CC) AppleWebKit/537.36"},
            {"OPPO", "Find X7", "14", "Mozilla/5.0 (Linux; Android 14; PHZ110) AppleWebKit/537.36"},
            {"vivo", "X100", "14", "Mozilla/5.0 (Linux; Android 14; V2309A) AppleWebKit/537.36"}
        };

        for (String[] device : devices) {
            ClickReportData data = generateAndroidClickData();
            data.brand = device[0];
            data.model = device[1];
            data.os_ver = device[2];
            data.ua = device[3];
            data.requestId = UUID.randomUUID().toString();

            System.out.println(device[0] + " " + device[1] + ":");
            System.out.println("  完整参数: reportType=" + data.reportType +
                             "&requestId=" + data.requestId +
                             "&brand=" + data.brand +
                             "&model=" + urlEncode(data.model) +
                             "&os_ver=" + data.os_ver);
            System.out.println();
        }
    }
}
