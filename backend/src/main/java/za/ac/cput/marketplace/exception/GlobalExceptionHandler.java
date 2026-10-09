package za.ac.cput.marketplace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<Map<String, Object>> handleAdminException(AdminException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("status", ex.getStatus().value());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage() != null ? ex.getMessage() : "";

        if (message.contains("Access denied") || message.contains("Not logged in")) {
            status = HttpStatus.FORBIDDEN;
        }

        return ResponseEntity.status(status).body(body);
    }
}