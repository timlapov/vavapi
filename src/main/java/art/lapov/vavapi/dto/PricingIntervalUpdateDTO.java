package art.lapov.vavapi.dto;

import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * DTO for {@link art.lapov.vavapi.model.PricingInterval}
 */
@Value
public class PricingIntervalUpdateDTO implements Serializable {
    @Positive
    Integer hourlyPriceInCents;
    LocalTime startHour;
    LocalTime endHour;
}