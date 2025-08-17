package art.lapov.vavapi.handler;

import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.exception.UserHasActiveReservationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserHasActiveReservationException.class)
    public ResponseEntity<Map<String, String>> handleUserHasActiveReservationException(
            UserHasActiveReservationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Cannot delete user",
                        "message", ex.getMessage(),
                        "code", "ACTIVE_RESERVATIONS_OR_STATIONS"
                ));
    }

}
