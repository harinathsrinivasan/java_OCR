package kapia.dev.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kapia.dev.ratelimiting.RateLimitingService;
import kapia.dev.util.IpResolverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final IpResolverService ipResolverService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    public RateLimitingFilter(RateLimitingService rateLimitingService, IpResolverService ipResolverService) {
        this.rateLimitingService = rateLimitingService;
        this.ipResolverService = ipResolverService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (!request.getRequestURI().equals("/getOCR")) {
            chain.doFilter(request, response);
            return;
        }

        if (hasAdminRole(request)) {
            LOGGER.info("Resolved rate limiting for ROLE_ADMIN");
            chain.doFilter(request, response);
            return;
        }
        if (hasApiKey(request)) {
            LOGGER.info("Trying to resolve limit for API key");
            if (canConsumeTokenWithKey(request, response)) {
                return;
            }
        } else if (hasValidIp(getIp(request))) {
            LOGGER.info("Trying to resolve limit for IP address");
            if (canConsumeTokenWithIp(request, response)) {
                return;
            }
        } else {
            LOGGER.info("Request did not have a valid API key or IP address");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean hasAdminRole(HttpServletRequest request) {
        return request.isUserInRole("ROLE_ADMIN");
    }

    private boolean hasApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("x-api-key");
        return apiKey != null && !apiKey.isEmpty();
    }

    private boolean canConsumeTokenWithKey(HttpServletRequest request, HttpServletResponse response) {
        Bucket bucket = rateLimitingService.resolveBucketFromKey(request.getHeader("x-api-key"));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return canConsumeToken(probe, response);
    }

    private String getIp(HttpServletRequest request) {
        return ipResolverService.extractIpFromRequest(request);
    }

    private boolean hasValidIp(String ip) {
        return ipResolverService.isIpAddressValid(ip);
    }

    private boolean canConsumeTokenWithIp(HttpServletRequest request, HttpServletResponse response) {
        String ip = getIp(request);
        if (ip != null && !ip.isEmpty()) {
            Bucket bucket = rateLimitingService.resolveBucketFromIp(ip);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            return canConsumeToken(probe, response);
        }
        return false;
    }

    private boolean canConsumeToken(ConsumptionProbe probe, HttpServletResponse response) {
        if (probe.isConsumed()) {
            LOGGER.info("Token consumed");
            response.addHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
            return false;
        } else {
            LOGGER.info("Limit exceeded");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", Long.toString(Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds()));
            return true;
        }
    }
}
