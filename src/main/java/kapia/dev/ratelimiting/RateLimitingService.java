package kapia.dev.ratelimiting;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingService.class);

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> cacheIp = new ConcurrentHashMap<>();

    public Bucket resolveBucketFromKey(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucketBasedOnKey);
    }

    public Bucket resolveBucketFromIp(String ip) {
        return cacheIp.computeIfAbsent(ip, this::newBucketBasedOnIp);
    }

    public Bucket newBucketBasedOnKey(String apiKey) {
        LOGGER.info("Creating new bucket for API key: " + apiKey);
        PricingPlan pricingPlan = PricingPlan.resolvePlanFromKey(apiKey);
        return Bucket.builder()
                .addLimit(pricingPlan.getLimit())
                .build();
    }

    public Bucket newBucketBasedOnIp(String ip) {
        LOGGER.info("Creating new bucket for IP: " + ip);
        return Bucket.builder()
                .addLimit(PricingPlan.FREE.getLimit())
                .build();
    }

}
