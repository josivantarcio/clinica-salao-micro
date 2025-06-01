package com.clinicsalon.loyalty.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException exception, WebRequest request) {
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .path(request.getDescription(false))
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception exception, WebRequest request) {
        
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message(exception.getMessage())
                .path(request.getDescription(false))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, 
            HttpStatus status, 
            WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        
        ValidationErrorDetails errorDetails = ValidationErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .message("Validation failed")
                .path(request.getDescription(false))
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    
    public static class ErrorDetails {
        private LocalDateTime timestamp;
        private String message;
        private String path;
        private int status;
        
        // Getters e Setters
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public int getStatus() {
            return status;
        }
        
        public void setStatus(int status) {
            this.status = status;
        }
        
        // Builder
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final ErrorDetails instance = new ErrorDetails();
            
            public Builder timestamp(LocalDateTime timestamp) {
                instance.setTimestamp(timestamp);
                return this;
            }
            
            public Builder message(String message) {
                instance.setMessage(message);
                return this;
            }
            
            public Builder path(String path) {
                instance.setPath(path);
                return this;
            }
            
            public Builder status(int status) {
                instance.setStatus(status);
                return this;
            }
            
            public ErrorDetails build() {
                return instance;
            }
        }
    }
    
    public static class ValidationErrorDetails extends ErrorDetails {
        private Map<String, String> errors;
        
        public Map<String, String> getErrors() {
            return errors;
        }
        
        public void setErrors(Map<String, String> errors) {
            this.errors = errors;
        }
        
        public static ValidationBuilder builder() {
            return new ValidationBuilder();
        }
        
        public static class ValidationBuilder extends Builder {
            private final ValidationErrorDetails instance = new ValidationErrorDetails();
            
            @Override
            public ValidationBuilder timestamp(LocalDateTime timestamp) {
                instance.setTimestamp(timestamp);
                return this;
            }
            
            @Override
            public ValidationBuilder message(String message) {
                instance.setMessage(message);
                return this;
            }
            
            @Override
            public ValidationBuilder path(String path) {
                instance.setPath(path);
                return this;
            }
            
            @Override
            public ValidationBuilder status(int status) {
                instance.setStatus(status);
                return this;
            }
            
            public ValidationBuilder errors(Map<String, String> errors) {
                instance.setErrors(errors);
                return this;
            }
            
            @Override
            public ValidationErrorDetails build() {
                return instance;
            }
        }
    }
}
