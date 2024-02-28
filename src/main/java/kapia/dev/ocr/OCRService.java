package kapia.dev.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class OCRService {

    final String TESSDATA_PATH = "tessdata";

    // Process the image
    public String processImage(MultipartFile image) throws IOException {

        if (image == null) throw new IllegalArgumentException("Error during image reading");

        byte[] imageArr = null;

        try {
            imageArr = image.getBytes();
        } catch (IOException e) {
            throw new IOException("Error during image reading");
        }

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
        } catch (Exception e) {
            throw new IOException("Error during image reading");
        }
    }
}
