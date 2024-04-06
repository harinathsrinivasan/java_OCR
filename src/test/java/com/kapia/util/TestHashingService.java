package com.kapia.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {HashingService.class})
@ExtendWith(SpringExtension.class)
public class TestHashingService {

    final String EXAMPLE_BASIC_KEY = "BASIC-8186f52b84ab726ed62ddaaeb8e9b7efd180f01e7721134a39d14f1b2de4045e";
    final String EXAMPLE_PRO_KEY = "PRO-8186f52b84ab726ed62ddaaeb8e9b7efd180f01e7721134a39d14f1b2de4045e";
    final String EXAMPLE_STRING = "some string";

    @Test
    public void givenString_whenHashed_thenReturnHash() {
        HashingService hashingService = new HashingService();
        String hashed = hashingService.hash(EXAMPLE_STRING);

        Assertions.assertTrue(hashed.length() > 0);
    }

    @Test
    public void givenString_whenHashed_thenReturnHashWithPrefixAndLength() {
        HashingService hashingService = new HashingService();
        String hashedBasic = hashingService.hashKey(EXAMPLE_BASIC_KEY);
        String hashedPro = hashingService.hashKey(EXAMPLE_PRO_KEY);

        Assertions.assertTrue(hashedBasic.startsWith("BA"));
        Assertions.assertTrue(hashedPro.startsWith("PR"));
        Assertions.assertTrue(hashedBasic.length() == 66);
        Assertions.assertTrue(hashedPro.length() == 66);
    }

}
