package com.kapia.redis;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);
    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Value("${REDIS_KEY_HOST:localhost}")
    private String redisKeyHost;

    @Value("${REDIS_KEY_PORT:6380}")
    private int redisKeyPort;

    private RedisClient redisClient() {
        LOGGER.info("Trying to connect to redis at {}:{}", redisHost, redisPort);

        return RedisClient.create(RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build());
    }

    @Bean
    public ProxyManager<String> lettuceProxyManager() {
        RedisClient redisClient = redisClient();
        StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1L)))
                .build();
    }

    @PreDestroy
    public void closeRedisClient() {
        LOGGER.info("Closing redis client");
        redisClient().shutdown();
    }

    public RedisClient redisKeyClient() {
        LOGGER.info("Trying to connect to redis-keystore at {}:{}", redisKeyHost, redisKeyPort);

        return RedisClient.create(RedisURI.builder()
                .withHost(redisKeyHost)
                .withPort(redisKeyPort)
                .build());
    }

    @Bean
    public StatefulRedisConnection<String, String> redisKeyConnection() {
        RedisClient redisKeyClient = redisKeyClient();
        StatefulRedisConnection<String, String> redisConnection = redisKeyClient.connect();

        return redisConnection;

    }

}