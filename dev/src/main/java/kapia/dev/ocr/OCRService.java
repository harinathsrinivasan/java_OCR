package kapia.dev.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class OCRService {

    // Process the image
    public String processImage(String image) {
        File file = new File(image);
        Tesseract tesseract = new Tesseract();
//        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        try {
            return tesseract.doOCR(file);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }
}
