package com.kapia.ratelimiting;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class RateLimitingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingService.class);

    @Qualifier("lettuceProxyManager")
    private final ProxyManager<String> proxyManager;
    private final Environment environment;

    @Autowired
    public RateLimitingService(Environment environment, ProxyManager<String> proxyManager) {
        this.environment = environment;
        this.proxyManager = proxyManager;
    }

    public boolean tryConsumeTokenWithKey(String key, HttpServletResponse response) {

        PricingPlan pricingPlan = PricingPlan.resolvePlanFromKey(key);
        Supplier<BucketConfiguration> bucketConfigurationSupplier = () -> getBucketConfigurationForPlan(pricingPlan);

        Bucket bucket = proxyManager.builder().build(key, bucketConfigurationSupplier);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            LOGGER.info("Token consumed for API key: " + key);
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            LOGGER.info("Token not consumed for API key: " + key);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            return false;
        }
    }

    public boolean tryConsumeTokenWithIp(String ip, HttpServletResponse response) {

        PricingPlan pricingPlan = PricingPlan.FREE;
        Supplier<BucketConfiguration> bucketConfigurationSupplier = () -> getBucketConfigurationForPlan(pricingPlan);

        Bucket bucket = proxyManager.builder().build(ip, bucketConfigurationSupplier);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            LOGGER.info("Token consumed for IP: " + ip);
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }
        LOGGER.info("Token not consumed for IP: " + ip);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
        return false;

    }

    private BucketConfiguration getBucketConfigurationForPlan(PricingPlan pricingPlan) {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.addLimit(pricingPlan.getLimit(environment));
        return configurationBuilder.build();

    }
}
