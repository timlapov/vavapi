package art.lapov.vavapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link art.lapov.vavapi.model.User}
 */
@Value
public class UserCreateDTO implements Serializable {
    @Email
    String email;
    @NotNull
    @Size(min = 8)
    String password;
    @NotBlank
    String firstName;
    @NotBlank
    String lastName;
    @NotBlank
    String phone;
    @NotBlank
    String address;
    @NotBlank
    String city;
    @NotBlank
    String country;
    @NotNull
    @Positive
    @Min(value = 10000)
    @Max(value = 99999)
    Integer postalCode;
}