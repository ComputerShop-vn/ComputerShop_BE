package sp26.group3.computer.sba301_computershop.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayConfig {
    @Value("${vnpay.pay-url}")
    String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    String vnp_ReturnUrl;

    @Value("${vnpay.tmn-code}")
    String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    String vnp_HashSecret;

    @Value("${vnpay.api-url}")
    String vnp_ApiUrl;

    @Value("${vnpay.version}")
    String vnp_Version;

    @Value("${vnpay.command}")
    String vnp_Command;

    @Value("${vnpay.order-type}")
    String orderType;

    @Value("${vnpay.ipn-url}")
    String vnp_IpnUrl;

    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                try {
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    sb.append(fieldValue);
                }
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
