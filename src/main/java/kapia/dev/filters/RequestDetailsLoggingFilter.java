package kapia.dev.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
@WebFilter(urlPatterns = "/getOCR")
public class RequestDetailsLoggingFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(RequestDetailsLoggingFilter.class.getName());

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        /*

         There may be multiple X-Forwarded-For headers present in a request. Additionally, the client can forge this header.
         Generally, this header cannot be trusted and might exploit the application (Log4Shell, for example).
         For the purpose of this project, only the first header is considered and the first IP address.

         The IP address is validated and checked for length, to avoid logging garbage.

        */

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null && !ipAddress.isEmpty()) {
            String[] ipAddresses = ipAddress.split(",");
            ipAddress = ipAddresses[0].trim();
            String ip4 = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$";
            String ip6 = "^(?:[A-F0-9]{1,4}:){7}[A-F0-9]{1,4}$";
            if ((!ipAddress.matches(ip4) && !ipAddress.matches(ip6)) || ipAddress.length() > 15) {
                ipAddress = httpRequest.getRemoteAddr();
            }
        } else {
            ipAddress = request.getRemoteAddr();
        }

        LOGGER.info(String.format("Request details: contentType=%s, contentLength=%d, clientIpAddress=%s",
                httpRequest.getContentType(),
                httpRequest.getContentLength(),
                ipAddress));

        chain.doFilter(request, response);
    }

}
