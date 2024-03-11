package kapia.dev.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kapia.dev.ratelimiting.RateLimitingService;
import kapia.dev.security.SecurityResolverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final SecurityResolverService securityResolverService;

    @Autowired
    public RateLimitingFilter(RateLimitingService rateLimitingService, SecurityResolverService securityResolverService) {
        this.rateLimitingService = rateLimitingService;
        this.securityResolverService = securityResolverService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (securityResolverService.hasRole("ROLE_ADMIN")) {
            System.out.println("Admin role detected, skipping rate limiting");
            chain.doFilter(request, response);
            return;
        }

        // Rate limit based on key
        if (request.getHeader("x-api-key") != null && !request.getHeader("x-api-key").isEmpty()) {
            System.out.println("API key detected, applying rate limit");
            String apiKey = request.getHeader("x-api-key");
            Bucket bucket = rateLimitingService.resolveBucketFromKey(apiKey);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            if (probe.isConsumed()) {
                System.out.println("API key rate limit consumed");
                System.out.println("Remaining tokens: " + probe.getRemainingTokens());
                response.addHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
                chain.doFilter(request, response);
            } else {
                System.out.println("API key rate limit exceeded");
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", Long.toString(Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds()));
            }
        }
        // If no API-key, then check rate limit based on IP
        else if (request.getRemoteAddr() != null && !request.getRemoteAddr().isEmpty()) {

            Bucket bucket = rateLimitingService.resolveBucketFromIp(request.getRemoteAddr());
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                System.out.println("IP rate limit consumed");
                System.out.println("Remaining tokens: " + probe.getRemainingTokens());
                response.addHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
                chain.doFilter(request, response);
            } else {
                System.out.println("IP rate limit exceeded");
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", Long.toString(Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds()));
            }
        }

    }

}
