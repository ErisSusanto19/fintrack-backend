package com.eris.fintrack.api.exception;

import com.eris.fintrack.api.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new ResponseEntity<>(ApiResponse.error("VALIDATION_ERROR", message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NoSuchElementException.class, RuntimeException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFoundExceptions(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            return new ResponseEntity<>(ApiResponse.error("NOT_FOUND", ex.getMessage()), HttpStatus.NOT_FOUND);
        }

        return handleAllExceptions(ex);
    }

    @ExceptionHandler({SecurityException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(Exception ex) {
        return new ResponseEntity<>(ApiResponse.error("ACCESS_DENIED", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {

        return new ResponseEntity<>(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error has occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}