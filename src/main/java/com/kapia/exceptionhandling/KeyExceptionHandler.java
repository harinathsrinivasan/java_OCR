package com.kapia.exceptionhandling;

import com.kapia.keys.KeyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {KeyController.class})
public class KeyExceptionHandler extends ResponseExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        LOGGER.info("Cannot register key: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid key type");
    }
}
