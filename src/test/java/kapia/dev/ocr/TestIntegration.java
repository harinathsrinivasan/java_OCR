package kapia.dev.ocr;

import jakarta.servlet.ServletContext;
import kapia.dev.DevApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DevApplication.class})
@WebAppConfiguration
@SpringBootTest(properties = "file.upload.content-type=image/jpeg,image/png")
public class TestIntegration {

    final static String OCR_ENDPOINT = "/getOCR";
    final static String CORRECT_RESPONSE = "It was the best of\n" +
            "times, it was the worst\n" +
            "of times, it was the age\n" +
            "of wisdom, it was the\n" +
            "age of foolishness...\n";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
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

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));

        String expectedResponse = CORRECT_RESPONSE;

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(expectedResponse, mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void givenImageJPEG_whenProcessImage_thenReturnText() throws Exception {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/jpeg", Files.readAllBytes(file.toPath()));

        String expectedResponse = CORRECT_RESPONSE;

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(expectedResponse, mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void givenImageJPG_whenProcessImage_thenReturnText() throws Exception {
        File file = new File("src/test/resources/sample_text_jpg.jpg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/jpeg", Files.readAllBytes(file.toPath()));

        String expectedResponse = CORRECT_RESPONSE;

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(expectedResponse, mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void givenImageOfIncorrectType_whenProcessImage_thenReturnBadRequest() throws Exception {

        File file = new File("src/test/resources/sample_text_bmp.bmp");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/bmp", Files.readAllBytes(file.toPath()));

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("File is not an image", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void givenIncorrectFile_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", "text.txt", "text/plain", "text".getBytes());

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("File is not an image", mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void givenEmptyRequest_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", null, null, (byte[]) null);

        MvcResult mvcResult = mockMvc.perform(multipart(OCR_ENDPOINT).file(multipartFile))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals("File is empty", mvcResult.getResponse().getContentAsString());

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

        assertEquals("File is not an image", mvcResult.getResponse().getContentAsString());

    }

}
