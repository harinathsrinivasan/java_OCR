package com.kapia.exceptionhandling;

import com.kapia.ocr.OCRController;
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
    private final static String ERROR_PROCESSING_IMAGE = "Error processing the image";
    private final static String ERROR_READING_IMAGE = "Error during image reading";
    private final static String MULTIPART_FILE_CANNOT_BE_NULL = "MultipartFile cannot be null";

    @ExceptionHandler(TesseractException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleProcessingException(TesseractException ex) {
        LOGGER.error(ERROR_PROCESSING_IMAGE, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_PROCESSING_IMAGE);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleIOException(IOException ex) {
        LOGGER.error(ERROR_READING_IMAGE, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_READING_IMAGE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.error(MULTIPART_FILE_CANNOT_BE_NULL, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MULTIPART_FILE_CANNOT_BE_NULL);
    }

    public static String getErrorProcessingImage() {
        return ERROR_PROCESSING_IMAGE;
    }

    public static String getErrorReadingImage() {
        return ERROR_READING_IMAGE;
    }

    public static String getMultipartFileCannotBeNull() {
        return MULTIPART_FILE_CANNOT_BE_NULL;
    }

}
