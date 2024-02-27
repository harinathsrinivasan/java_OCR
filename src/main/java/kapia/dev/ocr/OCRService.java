package kapia.dev.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Service
public class OCRService {

    final String TESSDATA_PATH = "tessdata";

    // Process the image
    public ResponseEntity<String> processImage(MultipartFile image) {
        
        // Convert MultipartFile to byte array
        byte[] imageArr = null;
        try {
            imageArr = image.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error during image reading", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Convert byte array to buffer image
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageArr));
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error during image reading", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);

        try {
            String result = tesseract.doOCR(bufferedImage);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (TesseractException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error during OCR processing", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
