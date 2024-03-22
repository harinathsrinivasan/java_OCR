package com.kapia.keys;

import com.kapia.ratelimiting.PricingPlan;
import com.kapia.util.HashingService;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KeyService {

    private final RedisCommands<String, String> redisCommands;

    private final RedisClient redisKeyClient;
    private final HashingService hashingService;

    @Autowired
    public KeyService(@Qualifier("redisKeyClient") RedisClient redisKeyClient, HashingService hashingService) {
        this.redisKeyClient = redisKeyClient;
        this.redisCommands = redisKeyClient.connect().sync();
        this.hashingService = new HashingService();
    }

    public boolean isKeyValid(String key) {
        getAllKeys();
        return redisCommands.exists(key) == 1;
    }

    public String addKey(String key) {
        String value = LocalDateTime.now().toString();
        redisCommands.set(key, value);
        return key;
    }

    public String generateKey(PricingPlan pricingPlan) {
        String salt = LocalDateTime.now().toString();
        String key = pricingPlan.name() + "-" + hashingService.hash(salt);
        String hashedKey = hashingService.hashKey(key);
        if (duplicateKeyCheck(hashedKey)) {
            return generateKey(pricingPlan);
        }
        addKey(hashedKey);
        return key;
    }

    public void getAllKeys() {
        System.out.println(redisCommands.keys("*"));
    }

    private boolean duplicateKeyCheck(String key) {
        return redisCommands.exists(key) == 1;
    }

}


/*
[PRac4e443457f69e38c99d29376279949ccdb61f5fd6c6cbaa8414af9567095e5f,
PR-201c07974ce88d473eaa320a2ee73d4f3b4f13e607ef629fa4eccb1728ee627cf6, key1,
PRc0b1e202956a8c609c6eb5d8d27ad3b711ebcae2c5e4f6679f7ea90f95e3d8ac, PR04a09fc68f6ebfbf79e9c0ff54c5574bdb3a47d04bfb1c924d2b7383ca7a3409,
PR30e4d6dab0a6674fbd372847075b75b725b5a307b3e9258697da9609eec94faf, PX48773f292a246e300b7b7736c07978122dd8bdc7b38990fbace846e3fd385611,
BAb648dc397c44603312bbaee6630dcb0c12d42fe3d46eed5e63dbe94d370fa261, BA3a531c48658a6ab71e01667c22d7cca43ce3e405001ad5bc59769a0790b31a6d,
BA6ddb51ccdb92c8e588573ed5d30e188cb2366b2157a1bed5c33e5f5205aaa4af, PX29899e03251dbcbb1afe1928020db98a78eed7330a348020f32a6f4cf4be9360]



 */