package art.lapov.vavapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.Location}
 */
@Value
public class LocationCreateDTO implements Serializable {
    @NotBlank
    String name;
    String description;
    @NotBlank
    String address;
    @NotBlank
    String city;
    @NotNull
    @Min(10000)
    @Max(99999)
    @Positive
    Integer postalCode;
    @NotBlank
    String country;
    Double latitude;
    Double longitude;
    String ownerId;
}