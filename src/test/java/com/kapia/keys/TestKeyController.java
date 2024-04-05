package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.redis.RedisKeyConfig;
import com.kapia.util.HashingService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
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

import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {KeyController.class, KeyService.class, HashingService.class, RedisKeyConfig.class})
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
public class TestKeyController {

    private static final String PRO_PLAN = "PRO";
    private static final String BASIC_PLAN = "BASIC";
    private static final String INVALID_PLAN = "INVALID";

    private static final String PRO_KEY = "PRO-123";
    private static final String BASIC_KEY = "BASIC-123";

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

        when(keyService.generateKeyAndAddToRedis(PricingPlan.valueOf(BASIC_PLAN))).thenReturn(BASIC_KEY);

        String key = keyController.getKey(BASIC_PLAN);

        Assertions.assertEquals(BASIC_KEY, key);

        verify(keyService, times(1)).generateKeyAndAddToRedis(PricingPlan.valueOf(BASIC_PLAN));

    }

    @Test
    public void givenProGetKey_whenGetKey_thenReturnsKey() {

        when(keyService.generateKeyAndAddToRedis(PricingPlan.valueOf(PRO_PLAN))).thenReturn(PRO_KEY);

        String key = keyController.getKey(PRO_PLAN);

        Assertions.assertEquals(PRO_KEY, key);

        verify(keyService, times(1)).generateKeyAndAddToRedis(PricingPlan.valueOf(PRO_PLAN));

    }

    @Test
    public void givenInvalidGetKeyR_whenGetKey_thenThrowsException() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> keyController.getKey(INVALID_PLAN));

        verify(keyService, never()).generateKeyAndAddToRedis(any());

    }

}
