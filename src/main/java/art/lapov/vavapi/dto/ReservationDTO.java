package art.lapov.vavapi.dto;

import art.lapov.vavapi.enums.ReservationStatus;
import art.lapov.vavapi.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link art.lapov.vavapi.model.Reservation}
 */
@Value
public class ReservationDTO implements Serializable {
    String id;
    ReservationStatus status;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Integer totalCostInCents;
    LocalDateTime createdAt;
    LocalDateTime acceptedAt;
    LocalDateTime paidAt;
    StationShortDTO station;
    UserShortDTO client;
    PaymentDTO payment;
    String reviewId;

    @JsonProperty("total_cost_euros")
    public Double getTotalCostEuros() {
        return totalCostInCents != null ? totalCostInCents / 100.0 : 0.0;
    }

    @JsonProperty("duration_hours")
    public Double getDurationHours() {
        if (startDate != null && endDate != null) {
            return java.time.Duration.between(startDate, endDate).toMinutes() / 60.0;
        }
        return 0.0;
    }

    @JsonProperty("can_cancel")
    public boolean canCancel() {
        // Can cancel if not completed/rejected and 24h before start
        return status != ReservationStatus.COMPLETED &&
                status != ReservationStatus.REJECTED &&
                status != ReservationStatus.CANCELLED &&
                LocalDateTime.now().isBefore(startDate.minusHours(24));
    }
}