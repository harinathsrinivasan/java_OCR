package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.redis.RedisKeyConfig;
import com.kapia.util.HashingService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {KeyController.class, KeyService.class, HashingService.class, RedisKeyConfig.class})
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
public class TestKeyController {

    @Container
    private static final RedisContainer REDIS_KEY_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379).withCommand("redis-server", "--loglevel", "debug");

    @DynamicPropertySource
    private static void registerRedisKeyProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.key.host", REDIS_KEY_CONTAINER::getHost);
        registry.add("redis.key.port", () -> REDIS_KEY_CONTAINER.getMappedPort(6379)
                .toString());
    }

    @Mock
    private HashingService hashingService;

    @Mock
    private KeyService keyService;

    @InjectMocks
    private KeyController keyController;

    @Test
    public void givenBasicGetKey_whenGetKey_thenReturnsKey() {

        when(keyService.generateKeyAndAddToRedis(PricingPlan.valueOf("BASIC"))).thenReturn("BASIC-123");

        String key = keyController.getKey("BASIC");

        assertEquals("BASIC-123", key);

    }

    @Test
    public void givenProGetKey_whenGetKey_thenReturnsKey() {

        when(keyService.generateKeyAndAddToRedis(PricingPlan.valueOf("PRO"))).thenReturn("PRO-123");

        String key = keyController.getKey("PRO");

        assertEquals("PRO-123", key);

    }

    @Test
    public void givenInvalidGetKeyR_whenGetKey_thenThrowsException() {

        assertThrows(IllegalArgumentException.class, () -> keyController.getKey("INVALID"));

    }

}
