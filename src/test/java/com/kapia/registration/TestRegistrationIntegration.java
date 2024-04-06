package com.kapia.registration;

import com.redis.testcontainers.RedisContainer;
import jakarta.transaction.Transactional;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
public class TestRegistrationIntegration {

    private static final String REGISTRATION_ENDPOINT = "/register";
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "password";
    private static final String VALID_AUTHORITY = "ROLE_ADMIN";

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

    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    private String createRequest(String username, String password, String authority) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        json.put("authority", authority);

        return json.toString();

    }

    @Test
    @Rollback
    public void givenRegistrationRequest_whenRegister_thenUserRegisteredSuccessfully() throws Exception {

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, VALID_AUTHORITY);

        System.out.println(request);

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());
    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithExistingUsername_whenRegister_thenUserNotRegistered() throws Exception {

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, VALID_AUTHORITY);

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithInvalidRole_whenRegister_thenUserNotRegistered() throws Exception {

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, "ROLE_INVALID");

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithForbiddenAuthority_whenRegister_thenUserNotRegistered() throws Exception {

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, "ROLE_SUPERUSER");

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithNoPassword_whenRegister_thenUserNotRegistered() throws Exception {

        String request = createRequest(VALID_USERNAME, "", VALID_AUTHORITY);

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithNoUsername_whenRegister_thenUserNotRegistered() throws Exception {

        String request = createRequest("", VALID_PASSWORD, VALID_AUTHORITY);

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    public void givenRegistrationRequest_whenLimitOfUsersReached_thenUserNotRegistered() throws Exception {

        for(int i = 0; i < 10; i++) {

            String request = createRequest(VALID_USERNAME + i, VALID_PASSWORD, VALID_AUTHORITY);

            mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)))
                    .andExpect(status().isCreated());
        }

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, VALID_AUTHORITY);

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());

    }

}
