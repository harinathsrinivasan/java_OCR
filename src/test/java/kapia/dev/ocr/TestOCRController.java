package kapia.dev.ocr;

import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TestOCRController {

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

        when(ocrService.processImage(any())).thenReturn(expectedText);

        ResponseEntity<String> response = ocrController.processImage(multipartFile);

        assertEquals(expectedText, response.getBody());
        assertEquals(expectedStatus, response.getStatusCode());

    }

    @Test
    public void givenIOException_whenProcessImage_thenReturnInternalServerError() throws IOException, TesseractException {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));

        when(ocrService.processImage(any())).thenThrow(new IOException());

        assertThrows(IOException.class, () -> ocrController.processImage(multipartFile));

    }

    @Test
    public void givenTesseractException_whenProcessImage_thenReturnInternalServerError() throws TesseractException, IOException {

        File file = new File("src/test/resources/sample_text_jpeg.jpeg");
        MockMultipartFile multipartFile = new MockMultipartFile("image", file.getName(), "image/png", Files.readAllBytes(file.toPath()));

        given(ocrService.processImage(any())).willAnswer(invocation -> {
            throw new TesseractException("Error processing the image");
        });

        assertThrows(TesseractException.class, () -> ocrController.processImage(multipartFile));

    }

}
