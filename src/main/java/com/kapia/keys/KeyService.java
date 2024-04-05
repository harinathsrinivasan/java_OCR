package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.util.HashingService;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.slf4j.LoggerFactory.*;

@Service
public class KeyService {

    @Value("${key.salt}")
    private String keySalt;

    private final static Logger LOGGER = getLogger(KeyService.class);

    private final RedisCommands<String, String> redisCommands;
    private final HashingService hashingService;

    @Autowired
    public KeyService(HashingService hashingService, StatefulRedisConnection<String, String> redisKeyConnection) {
        this.redisCommands = redisKeyConnection.sync();
        this.hashingService = hashingService;
    }

    private String addKey(String key) {
        LOGGER.info("Adding new key to Redis: " + hashingService.hashKey(key));
        String value = LocalDateTime.now().toString();
        redisCommands.set(key, value);
        return key;
    }

    private String generateRawKey(PricingPlan pricingPlan) {
        String rawKey;
        String hashedKey;
        do {
            rawKey = pricingPlan.name() + "-" + hashingService.hash(UUID.randomUUID().toString());
            hashedKey = hashingService.hashKey(rawKey);

        } while (doesExist(hashedKey));
        LOGGER.info("Generated new key for plan: " + pricingPlan.name());
        return rawKey;
    }

    private String hashRawKey(String rawKey) {
        String saltedKey = rawKey + keySalt;
        String hashedKey = hashingService.hashKey(saltedKey);
        return hashedKey;
    }

    public String generateKeyAndAddToRedis(PricingPlan pricingPlan) {
        String rawKey = generateRawKey(pricingPlan);
        String hashedKey = hashRawKey(rawKey);
        addKey(hashedKey);
        return rawKey;
    }

    private boolean doesExist(String hashedKey) {
        return redisCommands.exists(hashedKey) == 1;
    }

    public boolean isClientKeyValid(String rawKey) {
        String hashedKey = hashRawKey(rawKey);
        return doesExist(hashedKey);
    }

}