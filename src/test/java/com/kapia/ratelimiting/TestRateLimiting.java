package com.kapia.ratelimiting;

import com.kapia.filters.RateLimitingFilter;
import com.kapia.keys.KeyService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class TestRateLimiting {

    private static final String MULTIPART_PART_NAME = "image";

    @Value("${pricing.plans.free.limit.capacity}")
    private int FREE_PLAN_CAPACITY;

    @Value("${pricing.plans.basic.limit.capacity}")
    private int BASIC_PLAN_CAPACITY;

    @Value("${pricing.plans.pro.limit.capacity}")
    private int PRO_PLAN_CAPACITY;

    @Container
    private static final RedisContainer REDIS_BUCKET_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379).withCommand("redis-server", "--loglevel", "debug");

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.bucket.host", REDIS_BUCKET_CONTAINER::getHost);
        registry.add("redis.bucket.port", () -> REDIS_BUCKET_CONTAINER.getMappedPort(6379)
                .toString());
    }

    @Container
    private static final RedisContainer REDIS_KEY_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest")).withExposedPorts(6379).withCommand("redis-server", "--loglevel", "debug");

    @DynamicPropertySource
    private static void registerRedisKeyProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.key.host", REDIS_KEY_CONTAINER::getHost);
        registry.add("redis.key.port", () -> REDIS_KEY_CONTAINER.getMappedPort(6379)
                .toString());
    }

    @Container
    private static final MariaDBContainer<?> MARIADB_CONTAINER = new MariaDBContainer<>(DockerImageName.parse("mariadb:latest")).withDatabaseName("users_credentials").withUsername("ocr").withPassword("password");

    @DynamicPropertySource
    private static void registerMariaDBProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MARIADB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MARIADB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MARIADB_CONTAINER::getPassword);
    }

    final static String OCR_ENDPOINT = "/getOCR";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    private MockMvc mockMvc;

    @Autowired
    private KeyService keyService;

    private String PRO_KEY;

    private String BASIC_KEY;


    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).addFilter(rateLimitingFilter).build();
        PRO_KEY = keyService.generateKeyAndAddToRedis(PricingPlan.PRO);
        BASIC_KEY = keyService.generateKeyAndAddToRedis(PricingPlan.BASIC);
    }

    private MvcResult sendRequestWithKey(String key) throws Exception {

        File file = new File("src/test/resources/sample_text_png.png");
        MockMultipartFile multipartFile = new MockMultipartFile(MULTIPART_PART_NAME, file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        MockPart part = new MockPart("image", file.getName(), Files.readAllBytes(file.toPath()));
        part.getHeaders().setContentType(MediaType.IMAGE_PNG);

        return mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile).part(part).contentType(MediaType.MULTIPART_FORM_DATA).header("x-api-key", key))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void givenRequestWithNoKey_whenProcessRequest_thenTokenForIpIsConsumed() throws Exception {

        MockHttpServletResponse response = sendRequestWithKey("").getResponse();
        int remainingTokens = Integer.parseInt(Objects.requireNonNull(response.getHeader("x-rate-limit-remaining")));

        Assertions.assertEquals(FREE_PLAN_CAPACITY - 1, remainingTokens);

    }

    @Test
    public void givenRequestWithBasicKey_whenProcessRequest_thenTokenForIpIsConsumed() throws Exception {

        MockHttpServletResponse response = sendRequestWithKey(BASIC_KEY).getResponse();
        int remainingTokens = Integer.parseInt(Objects.requireNonNull(response.getHeader("x-rate-limit-remaining")));

        Assertions.assertEquals(BASIC_PLAN_CAPACITY - 1, remainingTokens);

    }

    @Test
    public void givenRequestWithProKey_whenProcessRequest_thenTokenForIpIsConsumed() throws Exception {

        MockHttpServletResponse response = sendRequestWithKey(PRO_KEY).getResponse();
        int remainingTokens = Integer.parseInt(Objects.requireNonNull(response.getHeader("x-rate-limit-remaining")));

        Assertions.assertEquals(PRO_PLAN_CAPACITY - 1, remainingTokens);

    }


}