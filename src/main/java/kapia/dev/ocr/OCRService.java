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
    public String processImage(MultipartFile image) throws IOException {
        
        // Convert MultipartFile to byte array
        byte[] imageArr = null;
        try {
            imageArr = image.getBytes();
        } catch (IOException e) {
            throw new IOException("Error during image reading");
        }

        // Convert byte array to buffer image
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new ByteArrayInputStream(imageArr));
        } catch (IOException e) {
            throw new IOException("Error during image reading");
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);

        try {
            return tesseract.doOCR(bufferedImage);
        } catch (TesseractException e) {
            throw new IOException("Error during OCR processing");
        }
    }
}
