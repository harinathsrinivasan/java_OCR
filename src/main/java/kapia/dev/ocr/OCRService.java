package kapia.dev.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class OCRService {

    final String TESSDATA_PATH = "tessdata";

    private static final Logger LOGGER = Logger.getLogger(OCRService.class.getName());

    public String processImage(MultipartFile image) throws IOException {

        BufferedImage bufferedImage;

        try {
            bufferedImage = convertToImage(image);
        } catch (IOException e) {
            LOGGER.severe("Error during image reading: " + e.getMessage());
            throw new IOException("Error during image reading");
        }

        try {
            return runTesseract(bufferedImage);
        } catch (Exception e) {
            LOGGER.severe("Error during OCR processing" + e.getMessage());
            throw new IOException("Error during image reading");
        }
    }

    private BufferedImage convertToImage(MultipartFile multipartFile) throws IOException {

        if (multipartFile == null) throw new IllegalArgumentException("MultipartFile cannot be null");
        byte[] array = multipartFile.getBytes();
        return ImageIO.read(new ByteArrayInputStream(array));

    }

    private String runTesseract(BufferedImage bufferedImage) throws TesseractException {

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        return tesseract.doOCR(bufferedImage);

    }
}
