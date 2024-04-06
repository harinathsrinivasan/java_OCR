package com.kapia.registration;

import com.redis.testcontainers.RedisContainer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Rollback
@Transactional
public class TestRegistrationService {

    private final static String VALID_ADMIN_ROLE = "ROLE_ADMIN";
    private final static String VALID_USERNAME = "admin";
    private final static String VALID_PASSWORD = "admin";

    @Autowired
    private RegistrationService registrationService;

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

    @Test
    public void givenRegistrationRequest_whenRegister_thenNoException() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME + LocalDateTime.now(), VALID_PASSWORD, VALID_ADMIN_ROLE);

        registrationService.register(request);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

    @Test
    public void givenRegistrationRequestWithInvalidRole_whenRegister_thenException() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, VALID_PASSWORD, "INVALID ROLE");

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

    @Test
    public void givenRegistrationRequestWithExistingUser_whenRegister_thenException() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, VALID_PASSWORD, VALID_ADMIN_ROLE);

        registrationService.register(request);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

    @Test
    public void givenRegistrationRequestWithLimitOfUsersReached_whenRegister_thenException() {

        for (int i = 0; i < 10; i++) {
            registrationService.register(new RegistrationRequest(VALID_USERNAME + i, VALID_PASSWORD, VALID_ADMIN_ROLE));
        }

        RegistrationRequest request = new RegistrationRequest("admin11", VALID_PASSWORD, VALID_ADMIN_ROLE);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

    @Test
    public void givenRegistrationRequestWithNoUsername_whenRegister_thenException() {
        RegistrationRequest request = new RegistrationRequest("", VALID_PASSWORD, VALID_ADMIN_ROLE);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

    @Test
    public void givenRegistrationRequestWithNoPassword_whenRegister_thenException() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, "", VALID_ADMIN_ROLE);

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

    @Test
    public void givenRegistrationRequestWithNoRole_whenRegister_thenException() {
        RegistrationRequest request = new RegistrationRequest(VALID_USERNAME, VALID_PASSWORD, "");

        Assertions.assertThrows(IllegalStateException.class, () -> registrationService.register(request));
    }

}
