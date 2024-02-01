package kapia.dev.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class OCRController {

    @Autowired
    OCRService ocrService;

    @PostMapping("/getOCR")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> processImage(@RequestParam("image") MultipartFile image) {

        try {
            byte[] imageArr = image.getBytes();
            ResponseEntity<String> response = ocrService.processImage(imageArr);
            System.out.println(response.getBody());
            return response;
        } catch (IOException e) {
            e.printStackTrace(); // You might want to log the exception instead of printing it
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image");
        }
    }
}

