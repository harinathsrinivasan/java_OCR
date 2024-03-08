package kapia.dev.ocr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@RestController
@Tag(name = "OCR", description = "OCR API")
public class OCRController {

    private final OCRService ocrService;

    private final static Logger LOGGER = Logger.getLogger(OCRController.class.getName());

    @Value("${file.upload.content-type}")
    private String contentTypes;

    @Autowired
    public OCRController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @Operation(summary = "Process the image", description = "Process the image and return the text")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image processed"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/getOCR", consumes = "multipart/form-data")
    public ResponseEntity<String> processImage(@RequestParam("image") @Parameter(name = "image", description = "Image to be processed") MultipartFile image) {

        List<String> allowedTypes = Arrays.asList(contentTypes.split(","));

        try {
            if (image == null || image.isEmpty()) {
                LOGGER.info("File was empty or null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }
            if (!image.getOriginalFilename().endsWith(".png") && !image.getOriginalFilename().endsWith(".jpeg") && !image.getOriginalFilename().endsWith(".jpg")) {
                LOGGER.info("File was not an image (wrong extension)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is not an image");
            }
            if (!allowedTypes.contains(image.getContentType())) {
                LOGGER.info("File is not an image (wrong content type)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is not an image");
            }
            return ResponseEntity.status(HttpStatus.OK).body(ocrService.processImage(image));
        } catch (Exception e) {
            LOGGER.severe("Error processing the image - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image");
        }
    }
}

