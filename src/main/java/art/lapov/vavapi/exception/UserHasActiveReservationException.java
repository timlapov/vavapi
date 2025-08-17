package art.lapov.vavapi.exception;

public class UserHasActiveReservationException extends RuntimeException {
    public UserHasActiveReservationException() {
        super("A user has active reservations and cannot be deleted");
    }

    public UserHasActiveReservationException(String message) {
        super(message);
    }
}
