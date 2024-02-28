package kapia.dev.ocr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(SpringRunner.class)
public class TestOCRService {

    @InjectMocks
    private OCRService ocrService;

    @Test
    public void givenImage_whenProcessImage_thenReturnText() throws IOException {

        File file = new File("src/test/resources/sample_text_png.png");
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        String expectedText =
                "It was the best of\n" +
                        "times, it was the worst\n" +
                        "of times, it was the age\n" +
                        "of wisdom, it was the\n" +
                        "age of foolishness...\n";

        String response = ocrService.processImage(multipartFile);

        assertEquals(expectedText, response);

    }

    @Test
    public void givenNoImage_whenProcessImage_thenThrowIllegalArgumentException() {

        Exception exception = assertThrows(IllegalArgumentException.class, () -> ocrService.processImage(null));

        assertEquals("Error during image reading", exception.getMessage());

    }

    @Test
    public void givenInvalidImageType_whenProcessImage_thenThrowIOException() {

        MultipartFile multipartFile = new MockMultipartFile("file", "file.txt", "text/plain", "some text".getBytes());

        Exception exception = assertThrows(IOException.class, () -> ocrService.processImage(multipartFile));

        assertEquals("Error during image reading", exception.getMessage());

    }

}
