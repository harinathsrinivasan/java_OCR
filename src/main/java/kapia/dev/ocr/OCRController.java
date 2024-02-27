package kapia.dev.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
public class OCRController {

    @Autowired
    OCRService ocrService;

    @Value("${file.upload.content-type}")
    private String contentTypes;

    @PostMapping(value = "/getOCR", consumes = "multipart/form-data")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<String> processImage(@RequestParam("image") MultipartFile image) {

        List<String> allowedTypes = Arrays.asList(contentTypes.split(","));

        try {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }
            if (!allowedTypes.contains(image.getContentType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is not an image");
            }
            return ResponseEntity.status(HttpStatus.OK).body(ocrService.processImage(image));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image");
        }
    }
}

