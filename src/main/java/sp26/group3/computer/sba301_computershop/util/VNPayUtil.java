package sp26.group3.computer.sba301_computershop.util;

import jakarta.servlet.http.HttpServletRequest;

public class VNPayUtil {

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "Invalid IP:" + e.getMessage();
        }
        return ipAddress;
    }

    public static String getRandomNumber(int len) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
