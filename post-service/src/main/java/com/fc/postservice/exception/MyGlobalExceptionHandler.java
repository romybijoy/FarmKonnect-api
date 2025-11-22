package com.fc.postservice.exception;

//import com.fc.postservice.controller.ReportController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    // Logger for this class
    private static final Logger log = LoggerFactory.getLogger(MyGlobalExceptionHandler.class);
    /**
     * Map IllegalStateException (e.g., duplicate pending report) to 409 Conflict.
     * You can expand/customize this for other domain exceptions.
     */
    @ExceptionHandler({ IllegalStateException.class, NoSuchElementException.class })
    public ResponseEntity<String> handleConflict(RuntimeException ex) {
        // choose message carefully to avoid leaking sensitive info
        log.info("Report operation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}
