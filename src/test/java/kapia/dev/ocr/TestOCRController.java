package kapia.dev.ocr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TestOCRController {

    private MockMvc mockMvc;

    @InjectMocks
    private OCRController ocrController;

    @Mock
    private OCRService ocrService;

    @Test
    public void givenImage_whenProcessImage_thenReturnResponse() throws Exception {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        String expectedText = "It was the best of\n" +
                "times, it was the worst\n" +
                "of times, it was the age\n" +
                "of wisdom, it was the\n" +
                "age of foolishness...\n";
        int expectedStatus = 200;
        org.springframework.test.util.ReflectionTestUtils.setField(ocrController, "contentTypes", "image/jpeg,image/png,image/jpeg");
        mockMvc = MockMvcBuilders.standaloneSetup(ocrController).build();

        when(ocrService.processImage(any())).thenReturn(expectedText);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/getOCR")
                        .file(multipartFile))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(expectedText, result.getResponse().getContentAsString());
        assertEquals(expectedStatus, result.getResponse().getStatus());

    }


    @Test
    public void givenEmptyRequest_whenProcessImage_thenReturnBadRequest() throws Exception {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", (byte[]) null);
        mockMvc = MockMvcBuilders.standaloneSetup(ocrController).build();

        int expectedStatus = 400;
        org.springframework.test.util.ReflectionTestUtils.setField(ocrController, "contentTypes", "image/jpeg,image/png,image/jpeg");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/getOCR")).andReturn();

        assertEquals(expectedStatus, result.getResponse().getStatus());

    }

    @Test
    public void givenIncorrectFile_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", "file.txt", "text/plain", "text".getBytes());

        int expectedStatus = 400;
        org.springframework.test.util.ReflectionTestUtils.setField(ocrController, "contentTypes", "image/jpeg,image/png,image/jpeg");
        mockMvc = MockMvcBuilders.standaloneSetup(ocrController).build();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/getOCR")).andReturn();

        assertEquals(expectedStatus, result.getResponse().getStatus());

    }

}
