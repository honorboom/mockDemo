package com.mock.service.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * UA 编码测试工具
 */
public class UAEncodeTest {

    public static void main(String[] args) {
        // 原始 UA
        String ua = "Dalvik/2.1.0 (Linux; U; Android 15; AGT-AN00 Build/HONORAGT-AN00)  com.hihonor.hos/10.0.4.303";

        System.out.println("========== UA 编码结果 ==========\n");

        System.out.println("【原始 UA】");
        System.out.println(ua);
        System.out.println();

        // URL 编码
        String encoded = URLEncoder.encode(ua, StandardCharsets.UTF_8);

        System.out.println("【编码后的 UA】");
        System.out.println(encoded);
        System.out.println();

        // 完整的 GET 请求示例
        System.out.println("【完整 GET 请求 URL 示例】");
        System.out.println("方式1 - 单独 ua 参数:");
        System.out.println("https://example.com/api?ua=" + encoded);
        System.out.println();

        System.out.println("方式2 - 配合 OCPX 点击上报参数:");
        String ocpxUrl = "https://advertiser.example.com/report?" +
                "reportType=6" +
                "&requestId=test-123456" +
                "&os=1" +
                "&imei=862123456789012" +
                "&imeiMd5=071df1152cda3260b02fcc9e1d0f8b2a" +
                "&callbackUrl=" + URLEncoder.encode("https://callback.yoyo.com/ocpx/transform", StandardCharsets.UTF_8) +
                "&ip=192.168.1.100" +
                "&ua=" + encoded +
                "&os_ver=15" +
                "&brand=HONOR" +
                "&model=" + URLEncoder.encode("AGT-AN00", StandardCharsets.UTF_8);

        System.out.println(ocpxUrl);
        System.out.println();

        // 编码规则说明
        System.out.println("【编码规则说明】");
        System.out.println("空格 ' '    → '+'  或  '%20'");
        System.out.println("斜杠 '/'    → '%2F'");
        System.out.println("冒号 ':'    → '%3A'");
        System.out.println("分号 ';'    → '%3B'");
        System.out.println("括号 '()'   → '%28' '%29'");
        System.out.println("点号 '.'    → '.' (不编码)");
        System.out.println();

        // 字符对照
        System.out.println("【关键字符编码对照】");
        String[] testChars = {" ", "/", ":", ";", "(", ")", "."};
        for (String ch : testChars) {
            System.out.println("'" + ch + "' → '" + URLEncoder.encode(ch, StandardCharsets.UTF_8) + "'");
        }
    }
}
