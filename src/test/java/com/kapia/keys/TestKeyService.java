package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.redis.RedisKeyConfig;
import com.kapia.util.HashingService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = {RedisKeyConfig.class, HashingService.class, KeyService.class})
public class TestKeyService {

    @Container
    private static final RedisContainer REDIS_BUCKET_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379).withCommand("redis-server", "--loglevel", "debug");

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.bucket.host", REDIS_BUCKET_CONTAINER::getHost);
        registry.add("redis.bucket.port", () -> REDIS_BUCKET_CONTAINER.getMappedPort(6379)
                .toString());
    }

    @Container
    private static final RedisContainer REDIS_KEY_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379).withCommand("redis-server", "--loglevel", "debug");

    @DynamicPropertySource
    private static void registerRedisKeyProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.key.host", REDIS_KEY_CONTAINER::getHost);
        registry.add("redis.key.port", () -> REDIS_KEY_CONTAINER.getMappedPort(6379)
                .toString());
    }

    @Autowired
    private KeyService keyService;

    @Test
    public void givenKeyService_whenGenerateKeyBasic_thenKeyIsGenerated() {
        String key = keyService.generateKeyAndAddToRedis(PricingPlan.BASIC);

        Assertions.assertTrue(key.length() > 0);
        Assertions.assertTrue(key.startsWith(PricingPlan.BASIC.name()));
    }

    @Test
    public void givenKeyService_whenGenerateKeyPro_thenKeyIsGenerated() {
        String key = keyService.generateKeyAndAddToRedis(PricingPlan.PRO);

        Assertions.assertTrue(key.length() > 0);
        Assertions.assertTrue(key.startsWith(PricingPlan.PRO.name()));
    }

    @Test
    public void givenValidKey_whenValidateKey_thenKeyIsValid() {
        String key = keyService.generateKeyAndAddToRedis(PricingPlan.BASIC);
        Assertions.assertTrue(keyService.doesExist(key));
    }

    @Test
    public void givenInvalidKey_whenValidateKey_thenKeyIsInvalid() {
        String key = keyService.generateKeyAndAddToRedis(PricingPlan.BASIC) + "invalid";
        Assertions.assertFalse(keyService.doesExist(key));
    }


}
