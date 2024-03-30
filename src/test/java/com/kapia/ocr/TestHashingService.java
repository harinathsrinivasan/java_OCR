package com.kapia.ocr;

import com.kapia.util.HashingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {HashingService.class})
@ExtendWith(SpringExtension.class)
public class TestHashingService {

    String EXAMPLE_BASIC_KEY = "BASIC-8186f52b84ab726ed62ddaaeb8e9b7efd180f01e7721134a39d14f1b2de4045e";
    String EXAMPLE_PRO_KEY = "PRO-8186f52b84ab726ed62ddaaeb8e9b7efd180f01e7721134a39d14f1b2de4045e";
    String EXAMPLE_STRING = "some string";

    @Test
    public void givenHashingService_whenInstantiated_thenNoExceptions() {
        HashingService hashingService = new HashingService();
    }

    @Test
    public void givenString_whenHashed_thenNoExceptions() {
        HashingService hashingService = new HashingService();
        String hashed = hashingService.hash(EXAMPLE_STRING);
    }

    @Test
    public void givenString_whenHashed_thenReturnHash() {
        HashingService hashingService = new HashingService();
        String hashed = hashingService.hash(EXAMPLE_STRING);
        assert hashed.length() > 0;
    }

    @Test
    public void givenString_whenHashed_thenReturnHashWithPrefixAndLength() {
        HashingService hashingService = new HashingService();
        String hashedBasic = hashingService.hashKey(EXAMPLE_BASIC_KEY);
        String hashedPro = hashingService.hashKey(EXAMPLE_PRO_KEY);
        assert hashedBasic.startsWith("BA");
        assert hashedPro.startsWith("PR");
        assert hashedBasic.length() == 66;
        assert hashedPro.length() == 66;
    }

}
