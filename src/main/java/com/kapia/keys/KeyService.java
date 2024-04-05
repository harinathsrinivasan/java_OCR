package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.util.HashingService;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KeyService {

    @Value("${key.salt}")
    private String keySalt;

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(KeyService.class);

    private final RedisCommands<String, String> redisCommands;
    private final HashingService hashingService;

    @Autowired
    public KeyService(HashingService hashingService, StatefulRedisConnection<String, String> redisKeyConnection) {
        this.redisCommands = redisKeyConnection.sync();
        this.hashingService = new HashingService();
    }

    private String addKey(String key) {
        String value = LocalDateTime.now().toString();
        redisCommands.set(key, value);
        return key;
    }

    private String generateRawKey(PricingPlan pricingPlan) {
        String rawKey = pricingPlan.name() + "-" + hashingService.hash(LocalDateTime.now().toString());

        String hashedKey = hashRawKey(rawKey);

        if (doesExist(hashedKey)) {
            return generateRawKey(pricingPlan);
        }

        return rawKey;
    }

    private String hashRawKey(String rawKey) {
        String saltedKey = rawKey + keySalt;
        return hashingService.hashKey(saltedKey);
    }

    public String generateKeyAndAddToRedis(PricingPlan pricingPlan) {
        String rawKey = generateRawKey(pricingPlan);
        String hashedKey = hashRawKey(rawKey);
        addKey(hashedKey);
        return rawKey;
    }

    public boolean doesExist(String key) {
        return redisCommands.exists(hashRawKey(key)) == 1;
    }

}