package kapia.dev.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Service
public class OCRService {

    final String TESSDATA_PATH = "src/main/resources/tessdata/";
    final String LANGUAGE = "eng";
    final int PAGE_SEG_MODE = 1;
    final int OCR_ENGINE_MODE = 1;
    final String IMAGE = "src/main/resources/test_picture.png";

    // For testing
    public OCRService() {
        System.out.println("OCRService created");
    }

    // Process the image
    public ResponseEntity<String> processImage(byte [] imageArr) {

        // Convert byte array to buffer image
        BufferedImage image = null;

        try {
            image = ImageIO.read(new ByteArrayInputStream(imageArr));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        File file = new File(IMAGE);
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage(LANGUAGE);
//      This causes errors
//      tesseract.setPageSegMode(PAGE_SEG_MODE);
//      tesseract.setOcrEngineMode(OCR_ENGINE_MODE);
        try {
            String response = tesseract.doOCR(file);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
