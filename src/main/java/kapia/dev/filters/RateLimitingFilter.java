package kapia.dev.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kapia.dev.ratelimiting.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Autowired
    public RateLimitingFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (hasAdminRole(request)) {
            chain.doFilter(request, response);
            return;
        }
        if (handleRateLimitingWithApiKey(request, response)) {
            return;
        }
        if (handleRateLimitingWithIp(request, response)) {
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean hasAdminRole(HttpServletRequest request) {
        return request.isUserInRole("ROLE_ADMIN");
    }

    private boolean handleRateLimitingWithApiKey(HttpServletRequest request, HttpServletResponse response) {
        String apiKey = request.getHeader("x-api-key");
        if (apiKey != null && !apiKey.isEmpty()) {
            Bucket bucket = rateLimitingService.resolveBucketFromKey(apiKey);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            return handleRateLimitResult(probe, response);
        }
        return false;
    }

    private boolean handleRateLimitingWithIp(HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        if (ip != null && !ip.isEmpty()) {
            Bucket bucket = rateLimitingService.resolveBucketFromIp(ip);
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            return handleRateLimitResult(probe, response);
        }
        return false;
    }

    private boolean handleRateLimitResult(ConsumptionProbe probe, HttpServletResponse response) {
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", Long.toString(Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds()));
        }
        return true;
    }
}
