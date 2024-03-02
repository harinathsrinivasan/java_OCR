package kapia.dev.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TestOCRService {

    @Autowired
    private OCRService ocrService;

    @BeforeEach
    public void init() {
        System.out.println("Initializing TestOCRService");
        ocrService = new OCRService();
    }

    @Test
    public void ocrServiceLoads() {
        System.out.println("Is OCRService instantiated? " + (ocrService != null));
        assertNotNull(ocrService);
    }

    @Test
    public void givenImage_whenProcessImage_thenReturnText() throws IOException {

        File file = new File("src/test/resources/sample_text_png.png");
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        String expectedText =
                """
                        It was the best of
                        times, it was the worst
                        of times, it was the age
                        of wisdom, it was the
                        age of foolishness...
                        """;

        String response = ocrService.processImage(multipartFile);

        assertEquals(expectedText, response);

    }

    @Test
    public void givenNoImage_whenProcessImage_thenThrowIllegalArgumentException() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> ocrService.processImage(null));

        assertEquals("MultipartFile cannot be null", exception.getMessage());

    }

    @Test
    public void givenInvalidImageType_whenProcessImage_thenThrowIOException() {

        MultipartFile multipartFile = new MockMultipartFile("file", "file.txt", "text/plain", "some text".getBytes());

        Exception exception = assertThrows(IOException.class, () -> ocrService.processImage(multipartFile));

        assertEquals("Error during image reading", exception.getMessage());

    }

}
