package kapia.dev.ocr;

import net.sourceforge.tess4j.ITesseract;
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

    final String TESSDATA_PATH = "tessdata";

    // Process the image
    public ResponseEntity<String> processImage(byte [] imageArr) {

        // Convert byte array to buffer image
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(imageArr));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error during image reading", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);

        try {
            String result = tesseract.doOCR(image);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (TesseractException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error during OCR processing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
