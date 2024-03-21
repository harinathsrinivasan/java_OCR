package com.kapia.exceptionhandling;

import com.kapia.registration.RegistrationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {RegistrationController.class})
public class RegistrationExceptionHandler extends ResponseExceptionHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationExceptionHandler.class);

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        LOGGER.warn("Cannot register user: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot register user: " + ex.getMessage());
    }

}
