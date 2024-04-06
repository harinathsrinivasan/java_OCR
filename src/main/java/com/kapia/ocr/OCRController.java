package com.kapia.ocr;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@OpenAPIDefinition(
        info = @Info(title = "OCR API", version = "0.1", description = "OCR API for processing images and extracting text from them."),
        tags = @Tag(name = "OCR", description = "OCR operations")
)
@SecurityRequirements({
        @SecurityRequirement(name = "basicAuth"), @SecurityRequirement(name = "x-api-key")
})
public class OCRController {

    private final OCRService ocrService;

    private final static Logger LOGGER = LoggerFactory.getLogger(OCRController.class);

    @Autowired
    public OCRController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @Operation(summary = "Process the image", description = "Endpoint that processes the image and returns the text found in it.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image processed", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "text/plain"))
    })
    @PostMapping(value = "/getOCR", consumes = "multipart/form-data", produces = "text/plain")
    public ResponseEntity<String> processImage(@RequestParam("image") @Parameter(name = "image", description = "Image to be processed") MultipartFile image) throws IOException, TesseractException {
        LOGGER.info("Processing image from request");
        return ResponseEntity.status(HttpStatus.OK).body(ocrService.processImage(image));

    }
}
