package com.mock.service.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * User-Agent 编码示例工具
 */
public class UAEncoderDemo {

    public static void main(String[] args) {
        // 常见的 User-Agent 示例
        String[] userAgents = {
            // Chrome on Windows
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",

            // iPhone Safari
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1",

            // Android Chrome
            "Mozilla/5.0 (Linux; Android 13; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",

            // Firefox
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",

            // Edge
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0"
        };

        System.out.println("========== User-Agent 编码示例 ==========\n");

        for (int i = 0; i < userAgents.length; i++) {
            String ua = userAgents[i];
            String encoded = encodeUA(ua);

            System.out.println("示例 " + (i + 1) + ":");
            System.out.println("原始 UA:");
            System.out.println(ua);
            System.out.println("\n编码后:");
            System.out.println(encoded);
            System.out.println("\nGET 请求示例:");
            System.out.println("http://example.com/api?ua=" + encoded);
            System.out.println("\n" + "=".repeat(80) + "\n");
        }
    }

    /**
     * 对 User-Agent 进行 URL 编码
     * @param ua 原始 User-Agent 字符串
     * @return URL 编码后的字符串
     */
    public static String encodeUA(String ua) {
        if (ua == null || ua.isEmpty()) {
            return "";
        }
        return URLEncoder.encode(ua, StandardCharsets.UTF_8);
    }

    /**
     * 解码 User-Agent
     * @param encodedUA 编码后的 UA
     * @return 原始 UA
     */
    public static String decodeUA(String encodedUA) {
        if (encodedUA == null || encodedUA.isEmpty()) {
            return "";
        }
        return java.net.URLDecoder.decode(encodedUA, StandardCharsets.UTF_8);
    }
}
