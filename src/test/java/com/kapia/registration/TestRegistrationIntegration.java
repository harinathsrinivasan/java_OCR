package com.kapia.registration;

import com.kapia.users.ApplicationUser;
import com.redis.testcontainers.RedisContainer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private static final String URL = "/register";

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @Rollback
    public void givenRegistrationRequest_whenRegister_thenUserRegisteredSuccessfully() throws Exception {

        String request = "{\"username\":\"testuser\",\"password\":\"password\",\"authority\":\"ROLE_ADMIN\"}";

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());
    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithExistingUsername_whenRegister_thenUserNotRegistered() throws Exception {

        String request = "{\"username\":\"testuser\",\"password\":\"password\",\"authority\":\"ROLE_ADMIN\"}";

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithInvalidRole_whenRegister_thenUserNotRegistered() throws Exception {

        String request = "{\"username\":\"testuser\",\"password\":\"password\",\"authority\":\"ROLE_USER\"}";

        mockMvc.perform((post(URL).content(request)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithForbiddenAuthority_whenRegister_thenUserNotRegistered() throws Exception {

        String request = "{\"username\":\"testuser\",\"password\":\"password\",\"authority\":\"ROLE_SUPERUSER\"}";

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithNoPassword_whenRegister_thenUserNotRegistered() throws Exception {
        String request = "{\"username\":\"testuser\",\"\":\"password\",\"authority\":\"ROLE_SUPERUSER\"}";

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    public void givenRegistrationRequestWithNoUsername_whenRegister_thenUserNotRegistered() throws Exception {
        String request = "{\"username\":\"\",\"\":\"password\",\"authority\":\"ROLE_SUPERUSER\"}";

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Rollback
    public void givenRegistrationRequest_whenLimitOfUsersReached_thenUserNotRegistered() throws Exception {

        for(int i = 0; i < 10; i++) {
            String request = "{\"username\":\"testuser" + i + "\",\"password\":\"password\",\"authority\":\"ROLE_ADMIN\"}";

            mockMvc.perform((post(URL).content(request)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)))
                    .andExpect(status().isCreated());
        }

        String request = "{\"username\":\"testuser\",\"password\":\"password\",\"authority\":\"ROLE_ADMIN\"}";

        mockMvc.perform((post(URL).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest());

    }

}
