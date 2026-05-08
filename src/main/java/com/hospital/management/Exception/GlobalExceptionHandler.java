package com.hospital.management.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.hospital.management.Exception.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
                return build(HttpStatus.NOT_FOUND, ex.getMessage());
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
                return build(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        @ExceptionHandler(AlreadyExistsException.class)
        public ResponseEntity<ApiError> handleAlreadyExists(AlreadyExistsException ex) {
                return build(HttpStatus.CONFLICT, ex.getMessage());
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
                return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccess(AccessDeniedException ex) {
                return build(HttpStatus.FORBIDDEN,
                                ex.getMessage() != null ? ex.getMessage() : "Bu işlem için yetkiniz bulunmamaktadır.");
        }

      
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
                String msg = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                return build(HttpStatus.BAD_REQUEST, msg);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGeneral(Exception ex) {
                ex.printStackTrace(); // loglama için
                return build(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmedik bir sistem hatası oluştu.");
        }

        private ResponseEntity<ApiError> build(HttpStatus status, String message) {
                return ResponseEntity.status(status)
                                .body(new ApiError(
                                                status.value(),
                                                message,
                                                LocalDateTime.now()));
        }
}