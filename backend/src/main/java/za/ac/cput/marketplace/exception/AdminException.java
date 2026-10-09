package za.ac.cput.marketplace.exception;

import org.springframework.http.HttpStatus;

public class AdminException extends RuntimeException {
    private final HttpStatus status;

    private AdminException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AdminException unauthorized(String message) {
        return new AdminException(HttpStatus.UNAUTHORIZED, message);
    }

    public static AdminException forbidden(String message) {
        return new AdminException(HttpStatus.FORBIDDEN, message);
    }

    public static AdminException notFound(String message) {
        return new AdminException(HttpStatus.NOT_FOUND, message);
    }

    public static AdminException conflict(String message) {
        return new AdminException(HttpStatus.CONFLICT, message);
    }
}
