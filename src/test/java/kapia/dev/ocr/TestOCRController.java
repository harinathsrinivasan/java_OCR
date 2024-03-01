package kapia.dev.ocr;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TestOCRController {

    final static String ACCEPTED_CONTENT_TYPES = "image/png,image/jpeg";

    @InjectMocks
    private OCRController ocrController;

    @Mock
    private OCRService ocrService;

    @Test
    public void givenImage_whenProcessImage_thenReturnResponse() throws Exception {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        String expectedText = """
                It was the best of
                times, it was the worst
                of times, it was the age
                of wisdom, it was the
                age of foolishness...
                """;
        HttpStatus expectedStatus = HttpStatus.OK;
        org.springframework.test.util.ReflectionTestUtils.setField(ocrController, "contentTypes", ACCEPTED_CONTENT_TYPES);

        when(ocrService.processImage(any())).thenReturn(expectedText);

        ResponseEntity<String> response = ocrController.processImage(multipartFile);

        assertEquals(expectedText, response.getBody());
        assertEquals(expectedStatus, response.getStatusCode());

    }

    @Test
    public void givenEmptyRequest_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", null, null, (byte[]) null);
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        org.springframework.test.util.ReflectionTestUtils.setField(ocrController, "contentTypes", ACCEPTED_CONTENT_TYPES);

        ResponseEntity<String> response = ocrController.processImage(multipartFile);

        assertEquals(expectedStatus, response.getStatusCode());

    }

    @Test
    public void givenIncorrectFile_whenProcessImage_thenReturnBadRequest() throws Exception {

        MockMultipartFile multipartFile = new MockMultipartFile("image", "file.txt", "text/plain", "text".getBytes());
        HttpStatus expectedStatus = HttpStatus.BAD_REQUEST;
        org.springframework.test.util.ReflectionTestUtils.setField(ocrController, "contentTypes", ACCEPTED_CONTENT_TYPES);

        ResponseEntity<String> response = ocrController.processImage(multipartFile);

        assertEquals(expectedStatus, response.getStatusCode());

    }

}
