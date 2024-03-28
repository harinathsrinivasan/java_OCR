package com.kapia.ocr;

import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ContextConfiguration(classes = {OCRService.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class TestOCRService {

    @Value("${tessdata.path:tessdata}")
    private String TESSDATA_PATH;

    @Autowired
    private OCRService ocrService;

    @BeforeEach
    public void init() {
        ocrService = new OCRService();
        org.springframework.test.util.ReflectionTestUtils.setField(ocrService, "TESSDATA_PATH", TESSDATA_PATH);
    }

    @Test
    public void givenImage_whenProcessImage_thenReturnText() throws IOException, IllegalArgumentException, TesseractException {

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

        assertThrows(IOException.class, () -> ocrService.processImage(null));

    }

    @Test
    public void givenInvalidImageType_whenProcessImage_thenThrowIOException() {

        MultipartFile multipartFile = new MockMultipartFile("file", "file.txt", "text/plain", "some text".getBytes());
        assertThrows(IOException.class, () -> ocrService.processImage(multipartFile));

    }

}
