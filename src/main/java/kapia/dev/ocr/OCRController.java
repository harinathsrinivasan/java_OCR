package kapia.dev.ocr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Tag(name = "OCR", description = "OCR API")
public class OCRController {

    private final OCRService ocrService;

    private final static Logger LOGGER = LoggerFactory.getLogger(OCRController.class);

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
    public ResponseEntity<String> processImage(@RequestParam("image") @Parameter(name = "image", description = "Image to be processed") MultipartFile image) throws IOException, TesseractException {

        return ResponseEntity.status(HttpStatus.OK).body(ocrService.processImage(image));

    }
}

