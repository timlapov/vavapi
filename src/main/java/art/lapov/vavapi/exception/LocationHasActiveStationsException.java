package art.lapov.vavapi.exception;

public class LocationHasActiveStationsException extends RuntimeException {
    public LocationHasActiveStationsException() {
        super("Location has active stations and cannot be deleted");
    }

    public LocationHasActiveStationsException(String message) {
        super(message);
    }
}