package art.lapov.vavapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.User}
 */
@Value
public class UserShortDTO implements Serializable {
    String id;
    @NotBlank
    String firstName;
    @NotBlank
    String lastName;
    String miniAvatarUrl;
}