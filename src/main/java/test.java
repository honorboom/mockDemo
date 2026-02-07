import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class test {


    public static <string> void main(String[] args) {
        String ua = "Mozilla/5.0 (Linux; Android 14; 23073RPBFC Build/UKQ1.231003.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/118.0.0.0 Safari/537.36";
        String encoded = URLEncoder.encode(ua, StandardCharsets.UTF_8);

        System.out.println(encoded);
    }
}
