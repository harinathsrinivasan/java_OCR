package kapia.dev.exceptionhandling;

import kapia.dev.ocr.OCRController;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

@ControllerAdvice(assignableTypes = {OCRController.class})
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseExceptionHandler.class);

    @ExceptionHandler(TesseractException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleProcessingException(TesseractException ex) {
        LOGGER.error("Error processing the image", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the image");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleIOException(IOException ex) {
        LOGGER.error("Error during image reading", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during image reading");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error("MultipartFile cannot be null", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MultipartFile cannot be null");
    }

}

