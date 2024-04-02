package com.kapia.keys;

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

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, "/key").param("plan", "BASIC"))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertNotNull(key);
        Assertions.assertTrue(key.startsWith("BASIC-"));

    }

    @Test
    public void givenProKeyRequest_whenRequesting_thenRequestIsSuccessful() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, "/key").param("plan", "PRO"))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertNotNull(key);
        Assertions.assertTrue(key.startsWith("PRO-"));

    }

    @Test
    public void givenInvalidKeRequest_whenRequesting_thenBadRequestIsReturned() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, "/key").param("plan", "INVALID"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(key.contains("Invalid key type"));

    }

    @Test
    public void givenBasicKeyRequest_whenRequesting_thenKeyIsAddedToRedis() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, "/key").param("plan", "BASIC"))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(keyService.doesExist(key));

    }

    @Test
    public void givenProKeyRequest_whenRequesting_thenKeyIsAddedToRedis() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, "/key").param("plan", "PRO"))
                .andExpect(status().isOk())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(keyService.doesExist(key));

    }

    @Test
    public void givenInvalidKeyRequest_whenRequesting_thenKeyIsNotAddedToRedis() throws Exception {

        MvcResult mvcResult = mockMvc.perform(request(HttpMethod.GET, "/key").param("plan", "INVALID"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String key = mvcResult.getResponse().getContentAsString();

        Assertions.assertTrue(key.contains("Invalid key type"));

    }

}