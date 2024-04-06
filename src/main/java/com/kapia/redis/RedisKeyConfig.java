package com.kapia.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisKeyConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisBucketConfig.class);

    @Value("${redis.key.host}")
    private String redisKeyHost;

    @Value("${redis.key.port}")
    private int redisKeyPort;

    @Value("${redis.key.password}")
    private char[] redisKeyPassword;

    public RedisClient redisKeyClient(String redisKeyHost, int redisKeyPort, char[] redisKeyPassword) {
        LOGGER.info("Creating a Redis client for host: {} and port: {}", redisKeyHost, redisKeyPort);

        return RedisClient.create(RedisURI.builder()
                .withHost(redisKeyHost)
                .withPort(redisKeyPort)
                .withPassword(redisKeyPassword)
                .build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> redisKeyConnection() {

        LOGGER.info("Creating a Redis StatefulRedisConnection for host: {} and port: {}", redisKeyHost, redisKeyPort);
        RedisClient redisClient = redisKeyClient(redisKeyHost, redisKeyPort, redisKeyPassword);

        return redisClient.connect();
    }

}