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
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisBucketConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisBucketConfig.class);

    @Value("${redis.bucket.host}")
    private String redisHost;

    @Value("${redis.bucket.port}")
    private int redisPort;

    @Value("${redis.bucket.password}")
    private char[] redisPassword;

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String,  byte[]> lettuceRedisConnection() {

        LOGGER.info("Creating a Redis StatefulRedisConnection for host: {} and port: {}", redisHost, redisPort);
        RedisClient redisClient = RedisClient.create(RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withPassword(redisPassword)
                .build());

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