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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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

    @Bean(destroyMethod = "shutdown")
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RedisClient redisClient(String redisHost, int redisPort) {
        LOGGER.info("Creating a Redis client for host: {} and port: {}", redisHost, redisPort);

        return RedisClient.create(RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> redisKeyConnection() {

        LOGGER.info("Creating a Redis StatefulRedisConnection for host: {} and port: {}", redisKeyHost, redisKeyPort);
        RedisClient redisClient = redisClient(redisKeyHost, redisKeyPort);

        return redisClient.connect();
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String,  byte[]> lettuceRedisConnection() {

        LOGGER.info("Creating a Redis StatefulRedisConnection for host: {} and port: {}", redisHost, redisPort);
        RedisClient redisClient = redisClient(redisHost, redisPort);

        return redisClient
                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
    }

    @Bean
    public ProxyManager<String> lettuceProxyManager() {

        StatefulRedisConnection<String, byte[]> redisConnection = lettuceRedisConnection();

        return LettuceBasedProxyManager.builderFor(redisConnection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1L)))
                .build();
    }

}