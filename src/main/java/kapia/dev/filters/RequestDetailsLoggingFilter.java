package kapia.dev.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(urlPatterns = "/getOCR")
public class RequestDetailsLoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDetailsLoggingFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        /*

         There may be multiple X-Forwarded-For headers present in a request and the client can forge this header.
         Generally, this header cannot be trusted and might exploit the application (Log4Shell).
         For the purpose of this project, only the first header is considered and the first IP address.

        */

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");

        if (validateIpAddress(ipAddress, httpRequest)) {
            ipAddress = hashIpAddress(ipAddress);
        } else {
            ipAddress = hashIpAddress(httpRequest.getRemoteAddr());
        }

        LOGGER.info(String.format("Request details: contentType=%s, contentLength=%d, clientIpAddress=%s",
                httpRequest.getContentType(),
                httpRequest.getContentLength(),
                ipAddress));

        chain.doFilter(request, response);
    }

    private String hashIpAddress(String ipAddress) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(ipAddress.getBytes());
            return new java.math.BigInteger(1, hash).toString(16);
        } catch (java.security.NoSuchAlgorithmException e) {
            LOGGER.error("Error while hashing IP address", e);
            return null;
        }
    }

    private boolean validateIpAddress(String ipAddress, HttpServletRequest request) {
        if (ipAddress != null && !ipAddress.isEmpty()) {
            String[] ipAddresses = ipAddress.split(",");
            ipAddress = ipAddresses[0].trim();
            String ip4 = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
            String ip6 = "^(?:[A-F0-9]{1,4}:){7}[A-F0-9]{1,4}$";
            return ipAddress.matches(ip4) || !ipAddress.matches(ip6);
        }
        return false;
    }
}
