package art.lapov.vavapi.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.Location}
 */
@Value
public class LocationShortDTO implements Serializable {
    String id;
    String name;
    String address;
    String city;
    Integer postalCode;
    Double latitude;
    Double longitude;
}