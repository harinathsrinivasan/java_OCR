package com.kapia.ocr;

import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {OCRController.class, OCRService.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class TestOCRController {

    private static final String PART_NAME = "image";
    private static final String FILE_PATH = "src/test/resources/sample_text_jpeg.jpeg";
    private static final String CONTENT_TYPE = "image/jpeg";

    @InjectMocks
    private OCRController ocrController;

    @Mock
    private OCRService ocrService;

    @Test
    public void givenImage_whenProcessImage_thenReturnResponse() throws Exception {

        File file = new File(FILE_PATH);
        MockMultipartFile multipartFile = new MockMultipartFile(PART_NAME, file.getName(), CONTENT_TYPE, Files.readAllBytes(file.toPath()));
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

        Assertions.assertEquals(expectedText, response.getBody());
        Assertions.assertEquals(expectedStatus, response.getStatusCode());

        verify(ocrService, times(1)).processImage(any());

    }

    @Test
    public void givenIOException_whenProcessImage_thenReturnInternalServerError() throws IOException, TesseractException {

        File file = new File(FILE_PATH);
        MockMultipartFile multipartFile = new MockMultipartFile(PART_NAME, file.getName(), CONTENT_TYPE, Files.readAllBytes(file.toPath()));

        when(ocrService.processImage(any())).thenThrow(new IOException());

        Assertions.assertThrows(IOException.class, () -> ocrController.processImage(multipartFile));

        verify(ocrService, times(1)).processImage(any());
    }

    @Test
    public void givenTesseractException_whenProcessImage_thenReturnInternalServerError() throws TesseractException, IOException {

        File file = new File(FILE_PATH);
        MockMultipartFile multipartFile = new MockMultipartFile(PART_NAME, file.getName(), CONTENT_TYPE, Files.readAllBytes(file.toPath()));

        given(ocrService.processImage(any())).willAnswer(invocation -> {
            throw new TesseractException("Error processing the image");
        });

        Assertions.assertThrows(TesseractException.class, () -> ocrController.processImage(multipartFile));

        verify(ocrService, times(1)).processImage(any());

    }

}
