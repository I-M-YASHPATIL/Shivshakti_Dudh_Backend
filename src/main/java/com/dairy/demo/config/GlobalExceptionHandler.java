package com.dairy.demo.config;

import com.dairy.demo.dto.DairyDTOs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex) {
                return ResponseEntity.notFound().build();
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<DairyDTOs.ApiError> handleRuntimeException(RuntimeException ex) {
                log.error("Runtime error: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new DairyDTOs.ApiError(ex.getMessage(), 400));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<DairyDTOs.ApiError> handleIllegalArgument(IllegalArgumentException ex) {
                log.error("Validation error: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new DairyDTOs.ApiError(ex.getMessage(), 400));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<DairyDTOs.ApiError> handleIllegalState(IllegalStateException ex) {
                log.error("State error: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new DairyDTOs.ApiError(ex.getMessage(), 409));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<DairyDTOs.ApiError> handleValidation(
                        MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult().getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", "));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new DairyDTOs.ApiError(message, 400));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<DairyDTOs.ApiError> handleGeneral(Exception ex) {
                log.error("Unexpected error: ", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new DairyDTOs.ApiError("Internal server error: " + ex.getMessage(), 500));
        }
}
