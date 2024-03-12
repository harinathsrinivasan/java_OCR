package kapia.dev.ratelimiting;

import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingService.class);

    /*

        The CHARS_TO_KEEP constant is used to determine
        how many characters of the API key or IP address should be kept in the logs.
        Ideally, sensitive data should be encrypted, hashed or redacted.

     */
    @Value("${logging.sensitive-data.max-chars}")
    private int CHARS_TO_KEEP;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final Map<String, Bucket> cacheIp = new ConcurrentHashMap<>();

    public Bucket resolveBucketFromKey(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucketBasedOnKey);
    }

    public Bucket resolveBucketFromIp(String ip) {
        return cacheIp.computeIfAbsent(ip, this::newBucketBasedOnIp);
    }

    public Bucket newBucketBasedOnKey(String apiKey) {
        LOGGER.info("Creating new bucket for API key: " + apiKey.substring(0, CHARS_TO_KEEP));
        PricingPlan pricingPlan = PricingPlan.resolvePlanFromKey(apiKey);
        return Bucket.builder()
                .addLimit(pricingPlan.getLimit())
                .build();
    }

    public Bucket newBucketBasedOnIp(String ip) {
        LOGGER.info("Creating new bucket for IP: " + ip.substring(0, CHARS_TO_KEEP));
        return Bucket.builder()
                .addLimit(PricingPlan.FREE.getLimit())
                .build();
    }

}
