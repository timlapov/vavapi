package art.lapov.vavapi.dto;

import art.lapov.vavapi.model.ConnectorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link art.lapov.vavapi.model.Station}
 */
@Value
public class StationDTO implements Serializable {
    String id;
    @Positive
    Long maxPowerWatt;
    @NotNull
    ConnectorType connectorType;
    String description;
    LocalDateTime createdAt;
    String fullPhotoUrl;
    String miniPhotoUrl;
}