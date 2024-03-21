package com.kapia.filters;

import com.kapia.ratelimiting.RateLimitingService;
import com.kapia.util.HashingService;
import com.kapia.util.IpResolverService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${admin.role.name:ROLE_ADMIN}")
    private String ROLE_ADMIN;

    @Value("${superuser.role.name:ROLE_SUPERUSER}")
    private String ROLE_SUPERUSER;

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

        if (hasPrivilegedRole(request)) {
            LOGGER.info("Resolved rate limiting for admin or superuser");
            chain.doFilter(request, response);
            return;
        }
        if (hasApiKey(request)) {
            String key = hashingService.hashKey(extractApiKey(request));
            LOGGER.info("Trying to resolve limit for API key: " + key);
            if (canConsumeTokenWithKey(response, key)) {
                return;
            }
        } else if (hasValidIp(ipResolverService.extractIpFromRequest(request))) {
            String ip = hashingService.hash(ipResolverService.extractIpFromRequestIfValid(request));
            LOGGER.info("Trying to resolve limit for IP address: " + ip);
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

    private boolean hasPrivilegedRole(HttpServletRequest request) {
        return request.isUserInRole(ROLE_ADMIN) || request.isUserInRole(ROLE_SUPERUSER);
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
