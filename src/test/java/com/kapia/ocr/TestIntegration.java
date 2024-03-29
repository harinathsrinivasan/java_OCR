package com.kapia.ocr;

import com.kapia.filters.FileValidationFilter;
import com.redis.testcontainers.RedisContainer;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.mock.web.MockServletContext;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
public class TestIntegration {

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

    final static String OCR_ENDPOINT = "/getOCR";
    final static String CORRECT_RESPONSE = """
            It was the best of
            times, it was the worst
            of times, it was the age
            of wisdom, it was the
            age of foolishness...
            """;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FileValidationFilter fileValidationFilter;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).addFilter(fileValidationFilter).build();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("OCRController"));

    }

    @Test
    public void givenImagePNG_whenProcessImage_thenReturnText() throws Exception {

        File file = new File("src/test/resources/sample_text_png.png");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        MockPart part = new MockPart("image", file.getName(), Files.readAllBytes(file.toPath()));
        part.getHeaders().setContentType(MediaType.IMAGE_PNG);

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile).part(part).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(CORRECT_RESPONSE, mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void givenImageJPEG_whenProcessImage_thenReturnText() throws Exception {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/jpeg", Files.readAllBytes(file.toPath()));
        MockPart part = new MockPart("image", file.getName(), Files.readAllBytes(file.toPath()));
        part.getHeaders().setContentType(MediaType.IMAGE_JPEG);

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile).part(part).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(CORRECT_RESPONSE, mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void givenImageJPG_whenProcessImage_thenReturnText() throws Exception {
        File file = new File("src/test/resources/sample_text_jpg.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/jpeg", Files.readAllBytes(file.toPath()));
        MockPart part = new MockPart("image", file.getName(), Files.readAllBytes(file.toPath()));
        part.getHeaders().setContentType(MediaType.IMAGE_JPEG);

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile).part(part).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(CORRECT_RESPONSE, mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void givenImageOfIncorrectType_whenProcessImage_thenReturnBadRequest() throws Exception {

        File file = new File("src/test/resources/sample_text_bmp.bmp");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/bmp", Files.readAllBytes(file.toPath()));

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();

    }

    @Test
    public void givenIncorrectFile_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", "text.txt", "text/plain", "text".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    public void givenEmptyRequest_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", null, null, (byte[]) null);

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();

    }

    /*

        Test the application against a spoofed content type of the image file.
        For example, if the file sent is a text file, but the content type of the file in the request is set to image/png.

     */

    @Test
    public void givenRequestWithSpoofedContentType_whenProcessImage_thenReturnBadRequest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("image", "name", "image/png", "text".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();

    }

}
