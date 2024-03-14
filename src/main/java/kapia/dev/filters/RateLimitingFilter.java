package kapia.dev.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kapia.dev.ratelimiting.RateLimitingService;
import kapia.dev.util.HashingService;
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
    private final HashingService hashingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    public RateLimitingFilter(RateLimitingService rateLimitingService, IpResolverService ipResolverService, HashingService hashingService) {
        this.rateLimitingService = rateLimitingService;
        this.ipResolverService = ipResolverService;
        this.hashingService = hashingService;
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
            String key = hashingService.hashKey(extractApiKey(request));
            if (canConsumeTokenWithKey(response, key)) {
                return;
            }
        } else if (hasValidIp(ipResolverService.extractIpFromRequest(request))) {
            String ip = hashingService.hash(ipResolverService.extractIpFromRequestIfValid(request));
            LOGGER.info("Trying to resolve limit for IP address");
            if (canConsumeTokenWithIp(response, ip)) {
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
        return request.isUserInRole("ROLE_ADMIN") || request.isUserInRole("ROLE_SUPERUSER");
    }

    private boolean hasApiKey(HttpServletRequest request) {
        String apiKey = extractApiKey(request);
        return apiKey != null && !apiKey.isEmpty();
    }

    private String extractApiKey(HttpServletRequest request) {
        return request.getHeader("x-api-key");
    }

    private boolean canConsumeTokenWithKey(HttpServletResponse response, String key) {
        Bucket bucket = rateLimitingService.resolveBucketFromKey(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return canConsumeToken(probe, response);
    }

    private String getIp(HttpServletRequest request) {
        return ipResolverService.extractIpFromRequest(request);
    }

    private boolean hasValidIp(String ip) {
        return ipResolverService.isIpAddressValid(ip);
    }

    private boolean canConsumeTokenWithIp(HttpServletResponse response, String ip) {
        Bucket bucket = rateLimitingService.resolveBucketFromIp(ip);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        return canConsumeToken(probe, response);
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
