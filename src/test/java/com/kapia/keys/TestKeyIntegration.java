package com.kapia.keys;

import com.kapia.exceptionhandling.KeyExceptionHandler;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class TestKeyIntegration {

    private static final String KEY_ENDPOINT = "/key";
    private static final String PRO_PLAN = "PRO";
    private static final String BASIC_PLAN = "BASIC";
    private static final String INVALID_PLAN = "INVALID";
    private static final String PARAMETER_NAME = "plan";

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
    private static final MariaDBContainer MARIADB_CONTAINER = new MariaDBContainer(DockerImageName.parse("mariadb:latest")).withDatabaseName("users_credentials").withUsername("ocr").withPassword("password");

    @DynamicPropertySource
    private static void registerMariaDBProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MARIADB_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MARIADB_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MARIADB_CONTAINER::getPassword);
    }

    @Autowired
    private KeyService keyService;

    @Autowired
    KeyController keyController;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void givenBasicKeyRequest_whenRequesting_thenRequestIsSuccessful() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT).param(PARAMETER_NAME, BASIC_PLAN))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertNotNull(key);
        Assertions.assertTrue(key.startsWith(BASIC_PLAN));

    }

    @Test
    public void givenProKeyRequest_whenRequesting_thenRequestIsSuccessful() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT).param(PARAMETER_NAME, PRO_PLAN))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertNotNull(key);
        Assertions.assertTrue(key.startsWith(PRO_PLAN));

    }

    @Test
    public void givenInvalidKeRequest_whenRequesting_thenBadRequestIsReturned() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT).param(PARAMETER_NAME, INVALID_PLAN))
                .andExpect(status().isBadRequest())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(key.contains(KeyExceptionHandler.getInvalidKeyMessage()));

    }

    @Test
    public void givenBasicKeyRequest_whenRequesting_thenKeyIsAddedToRedis() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT).param(PARAMETER_NAME, BASIC_PLAN))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(keyService.doesExist(key));

    }

    @Test
    public void givenProKeyRequest_whenRequesting_thenKeyIsAddedToRedis() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT).param(PARAMETER_NAME, PRO_PLAN))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(keyService.doesExist(key));

    }

    @Test
    public void givenInvalidKeyRequest_whenRequesting_thenKeyIsNotAddedToRedis() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT).param(PARAMETER_NAME, INVALID_PLAN))
                .andExpect(status().isBadRequest())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(key.contains(KeyExceptionHandler.getInvalidKeyMessage()));

    }

}