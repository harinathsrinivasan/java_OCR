package com.kapia.security;

import com.kapia.registration.RegistrationRequest;
import com.redis.testcontainers.RedisContainer;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
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

import java.io.File;
import java.nio.file.Files;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
public class TestSecurity {

    private final static String OCR_ENDPOINT = "/getOCR";
    private final static String KEY_ENDPOINT = "/key";
    private final static String REGISTRATION_ENDPOINT = "/register";
    private final static String OPENAPI_ENDPOINT = "/v3/api-docs";
    private final static String SWAGGER_UI_ENDPOINT = "/swagger-ui.html";
    private final static String ACTUATOR_ENDPOINT = "/actuator";

    private final String VALID_USERNAME = "username";
    private final String VALID_PASSWORD = "password";
    private final static String ADMIN_AUTHORITY = "ROLE_ADMIN";
    private final static String SUPERUSER_AUTHORITY = "ROLE_SUPERUSER";

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
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).apply(springSecurity(springSecurityFilterChain)).build();
    }

    private String createRequest(String username, String password, String authority) throws JSONException {

        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);
        json.put("authority", authority);

        return json.toString();

    }

    @Test
    @WithAnonymousUser
    public void givenOpenApiRequest_whenGetOpenApi_thenReturnsOpenApi() throws Exception {

        mockMvc.perform(request(HttpMethod.GET, OPENAPI_ENDPOINT))
                .andExpect(status().isOk());

    }

    @Test
    @WithAnonymousUser
    public void givenSwaggerUiRequest_whenGetSwaggerUi_thenReturnsSwaggerUi() throws Exception {

        mockMvc.perform(request(HttpMethod.GET, "/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

    }

    @Test
    @WithAnonymousUser
    public void givenOcrRequest_whenGetOcr_thenReturnsOcr() throws Exception {

        File file = new File("src/test/resources/sample_text_png.png");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        MockPart part = new MockPart("image", file.getName(), Files.readAllBytes(file.toPath()));
        part.getHeaders().setContentType(MediaType.IMAGE_PNG);

        mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile).part(part).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithMockUser(authorities = {ADMIN_AUTHORITY, SUPERUSER_AUTHORITY})
    public void givenRegistrationRequestWithAuthorisation_whenRegister_thenUserRegisteredSuccessfully() throws Exception {

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, ADMIN_AUTHORITY);

        mockMvc.perform((post(REGISTRATION_ENDPOINT).content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithAnonymousUser
    public void givenRegistrationRequestWithoutAuthorisation_whenRegister_thenUserNotRegistered() throws Exception {

        String request = createRequest(VALID_USERNAME, VALID_PASSWORD, ADMIN_AUTHORITY);

        mockMvc.perform(request(HttpMethod.POST, REGISTRATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {ADMIN_AUTHORITY, SUPERUSER_AUTHORITY})
    public void givenKeyRequestWithAuthorisation_whenGetKey_thenReturnsKey() throws Exception {

        String PLAN = "FREE";

        mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT)
                .param("plan", PLAN))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void givenKeyRequestWithNoAuthorisation_whenGetKey_thenReturnsUnauthorized() throws Exception {

        String PLAN = "FREE";

        mockMvc.perform(request(HttpMethod.GET, KEY_ENDPOINT)
                .param("plan", PLAN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {ADMIN_AUTHORITY, SUPERUSER_AUTHORITY})
    public void givenActuatorRequestWithAuthorisation_whenGetActuator_thenReturnsActuator() throws Exception {

            mockMvc.perform(request(HttpMethod.GET, ACTUATOR_ENDPOINT))
                    .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void givenActuatorRequestWithNoAuthorisation_whenGetActuator_thenReturnsUnauthorized() throws Exception {

        mockMvc.perform(request(HttpMethod.GET, ACTUATOR_ENDPOINT))
                .andExpect(status().isUnauthorized());

    }
}
