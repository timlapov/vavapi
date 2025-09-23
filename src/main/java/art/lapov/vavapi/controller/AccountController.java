package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.SimpleMessageDTO;
import art.lapov.vavapi.dto.UserCreateDTO;
import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.dto.UserUpdateDTO;
import art.lapov.vavapi.dto.UserUpdatePasswordDTO;
import art.lapov.vavapi.exception.UserHasActiveReservationException;
import art.lapov.vavapi.mapper.UserMapper;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
class AccountController {
    private final AccountService accountService;
    private final UserMapper userMapper;

    @Value("${app.frontend.login.url}")
    private String loginUrl;

    /**
     * Registers a new user.
     * @param dto the user data
     * @return the created user
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@RequestBody @Valid UserCreateDTO dto) {
        User user = accountService.register(userMapper.map(dto));
        return userMapper.map(user);
    }

    /**
     * Activates a user account.
     * @param token the activation token
     * @return a redirect to the login page
     */
    @GetMapping("/validate/{token}")
    public ResponseEntity<Void> activate(@PathVariable String token) {
        accountService.activateUser(token);
        URI redirectUri = URI.create(loginUrl + "?activated=true");
        // 302 FOUND + Location
        return ResponseEntity.status(302)
                .location(redirectUri)
                .build();
    }

    /**
     * Deletes a user.
     * Only the user themselves can perform this action.
     *
     * @param id The email of the user to delete.
     * @param principal The authenticated user.
     * @return A response entity with a success message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal User principal, @PathVariable String id) {
        if (!principal.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        try {
            accountService.deleteUser(id);
        } catch (Exception e) {
            throw new UserHasActiveReservationException("User has active reservations on their stations");
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@AuthenticationPrincipal User principal, @PathVariable String id, @RequestBody UserUpdateDTO userDto) {
        if (!principal.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        User user = accountService.updateUser(userDto);
        return userMapper.map(user);
    }

    /**
     * Resets the password for a user.
     * @param email the user's email
     * @return a redirect to the login page
     */
    @PostMapping("/password/{email}")
    public ResponseEntity<Void> resetPassword(@PathVariable String email) {
        accountService.resetPassword(email);
        URI redirectUri = URI.create(loginUrl + "?password_reset=true");
        return ResponseEntity.status(200)
                .location(redirectUri)
                .build();
    }

    /**
     * Updates the password for the currently authenticated user.
     * @param user the authenticated user
     * @param dto the new password
     * @return a confirmation message
     */
    @PatchMapping("/password")
    public SimpleMessageDTO updatePassword(@AuthenticationPrincipal User user, @RequestBody UserUpdatePasswordDTO dto) {
        accountService.updatePassword(user, dto.getNewPassword());
        return new SimpleMessageDTO("Password updated");
    }

    @GetMapping("/me")
    public UserDTO getMe(@AuthenticationPrincipal User user) {
        return userMapper.map(user);
    }

}
