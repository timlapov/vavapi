package art.lapov.vavapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * DTO for {@link art.lapov.vavapi.model.PricingInterval}
 */
@Value
public class PricingIntervalDTO implements Serializable {
    String id;
    @Positive
    Integer hourlyPriceInCents;
    LocalTime startHour;
    LocalTime endHour;
    String stationId;

    @JsonProperty("hourly_price_euros")
    public Double getHourlyPriceEuros() {
        return hourlyPriceInCents != null ? hourlyPriceInCents / 100.0 : 0.0;
    }
}