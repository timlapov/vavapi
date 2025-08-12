package art.lapov.vavapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link art.lapov.vavapi.model.Reservation}
 */
@Value
public class ReservationCreateDTO implements Serializable {
    @NotNull
    String stationId;

    @NotNull
    LocalDateTime startDate;

    @NotNull
    LocalDateTime endDate;
}