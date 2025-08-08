package art.lapov.vavapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.User}
 */
@Value
public class UserDTO implements Serializable {
    String id;
    String role;
    @Email
    String email;
    String fullName;
    @NotBlank
    String phone;
    @NotBlank
    String address;
    @NotBlank
    String city;
    @NotBlank
    String country;
    @NotNull
    @Min(10000)
    @Max(99999)
    @Positive
    Integer postalCode;
    String photoUrl;
    String fullAvatarUrl;
    String miniAvatarUrl;
    Boolean validated;
    Boolean deleted;
}