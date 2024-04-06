package com.kapia.filters;

import com.kapia.keys.KeyService;
import com.kapia.ratelimiting.RateLimitingService;
import com.kapia.util.HashingService;
import com.kapia.util.IpResolverService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final IpResolverService ipResolverService;
    private final HashingService hashingService;
    private final KeyService keyService;
    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${admin.authority.name}")
    private String ROLE_ADMIN;

    @Value("${superuser.authority.name}")
    private String ROLE_SUPERUSER;

    @Autowired
    public RateLimitingFilter(RateLimitingService rateLimitingService, IpResolverService ipResolverService, HashingService hashingService, KeyService keyService) {
        this.rateLimitingService = rateLimitingService;
        this.ipResolverService = ipResolverService;
        this.hashingService = hashingService;
        this.keyService = keyService;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException {

        if (!request.getRequestURI().equals("/getOCR")) {
            chain.doFilter(request, response);
            return;
        }
        if (hasPrivilegedRole(request)) {
            LOGGER.info("Resolved rate limiting for admin or superuser");
            chain.doFilter(request, response);
            return;
        }
        if (hasValidKey(request)) {
            String key = hashingService.hashKey(extractApiKey(request));
            LOGGER.info("Trying to resolve limit for API key: " + key);
            if (!canConsumeTokenWithKey(response, key)) {
                LOGGER.info("Over limit for API key: " + key);
                return;
            }
            LOGGER.info("Resolved rate limiting for API key: " + key);
        } else if (hasValidIp(ipResolverService.extractIpFromRequest(request))) {
            String ip = hashingService.hash(ipResolverService.extractIpFromRequestIfValid(request));
            LOGGER.info("Trying to resolve limit for IP address: " + ip);
            if (!canConsumeTokenWithIp(response, ip)) {
                LOGGER.info("Over limit for IP address: " + ip);
                return;
            }
            LOGGER.info("Resolved rate limiting for IP address: " + ip);
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

    private boolean hasValidKey(HttpServletRequest request) {
        String rawClientKey = extractApiKey(request);
        if (rawClientKey != null && !rawClientKey.isEmpty()) {
            LOGGER.info("Checking if API key is valid");
            String hashedKey = hashingService.hashKey(rawClientKey);
            boolean isValid = keyService.isClientKeyValid(rawClientKey);
            if (isValid) {
                LOGGER.info("API key is valid: " + hashedKey);
                return true;
            }
            LOGGER.info("API key is not valid: " + hashedKey);
        }
        return false;
    }

    private String extractApiKey(HttpServletRequest request) {
        return request.getHeader("x-api-key");
    }

    private boolean hasValidIp(String ip) {
        return ipResolverService.isIpAddressValid(ip);
    }

    private boolean canConsumeTokenWithKey(HttpServletResponse response, String key) {
        return rateLimitingService.tryConsumeTokenWithKey(key, response);
    }

    private boolean canConsumeTokenWithIp(HttpServletResponse response, String ip) {
        return rateLimitingService.tryConsumeTokenWithIp(ip, response);
    }

}
