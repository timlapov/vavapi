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
public class UserUpdateDTO implements Serializable {
    String id;
    @Email
    String email;
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
    @Min(10000)
    @Max(99999)
    @Positive
    Integer postalCode;
}