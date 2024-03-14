package kapia.dev.util;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.stereotype.Service;

@Service
public class IpResolverService {

    /*

        HttpServletRequest.getRemoteAddr() will return the address of the last proxy,
        or load balancer through which the request was sent.
        X-Forwarded-For will return the original address,
        from which the request was first sent,
        followed with the address of the proxy or the load balancer.

        There may be multiple X-Forwarded-For headers present in a request and the client can forge this header.
        Generally, this header cannot be trusted and might exploit the application (Log4Shell).
        For the purpose of this project, only the first header is considered and the first IP address.

     */

    public String extractIpFromRequest(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    public String extractIpFromRequestIfValid(HttpServletRequest request) {
        String ip = extractIpFromRequest(request);
        return isIpAddressValid(ip) ? ip : null;
    }

    public boolean isIpAddressValid(String ip) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        return validator.isValidInet4Address(ip) || validator.isValidInet6Address(ip);
    }

}
