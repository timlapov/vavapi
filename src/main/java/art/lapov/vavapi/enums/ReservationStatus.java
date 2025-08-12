package art.lapov.vavapi.enums;

public enum ReservationStatus {
    /**
     * Reservation created, waiting for owner approval
     */
    CREATED,

    /**
     * Reservation accepted by owner, waiting for payment confirmation
     */
    ACCEPTED,

    /**
     * Reservation rejected by owner
     */
    REJECTED,

    /**
     * Reservation cancelled by client
     */
    CANCELLED,

    /**
     * Payment confirmed, reservation is active
     */
    PAID,

    /**
     * Reservation completed successfully
     */
    COMPLETED
}
