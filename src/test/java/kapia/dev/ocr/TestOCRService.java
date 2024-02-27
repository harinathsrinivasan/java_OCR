package kapia.dev.ocr;

import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TestOCRService {

    @InjectMocks
    private OCRService ocrService;

    @Test
    public void givenImage_whenProcessImage_thenReturnText() throws IOException, TesseractException {

        File file = new File("src/test/resources/sample_text_png.png");
        MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(file.toPath()));
        String expectedText = "It was the best of\n times, it was the worst\n of times, it was the age\n of wisdom, it was the\n age of foolishness...";
        OCRService ocrService = Mockito.mock(OCRService.class);

        when(ocrService.processImage(multipartFile)).thenReturn(expectedText);

        assertEquals(expectedText, ocrService.processImage(multipartFile));

    }

    @Test
    public void givenNoImage_whenProcessImage_thenThrowIOException() throws IOException {

        OCRService ocrService = Mockito.mock(OCRService.class);
        MultipartFile multipartFile = null;

        try {
            when(ocrService.processImage(multipartFile)).thenThrow(new IOException("Error during image reading"));
        } catch (IOException e) {
            assertEquals("Error during image reading", e.getMessage());
        }

    }

    @Test
    public void givenInvalidImageType_whenProcessImage_thenThrowIOException() {

        OCRService ocrService = Mockito.mock(OCRService.class);
        MultipartFile multipartFile = new MockMultipartFile("file", "file.txt", "text/plain", "some text".getBytes());

        try {
            when(ocrService.processImage(multipartFile)).thenThrow(new IOException("Error during image reading"));
        } catch (IOException e) {
            assertEquals("Error during image reading", e.getMessage());
        }

    }

}
