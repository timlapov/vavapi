package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.LoginCredentialsDTO;
import art.lapov.vavapi.dto.LoginResponseDTO;
import art.lapov.vavapi.dto.SimpleMessageDTO;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.security.TokenPair;
import art.lapov.vavapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class AuthController {
    private final AuthService authService;

    @Value("${app.secure-cookies}")
    private boolean secureCookies;

    /**
     * Handles user login.
     *
     * @param credentials The login credentials.
     * @return A response entity containing the login response DTO and a refresh token cookie.
     */
    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginCredentialsDTO credentials) {
        LoginResponseDTO responseDto = authService.login(credentials);
        String refreshToken = authService.generateRefreshToken(responseDto.getUser().getId());
        ResponseCookie refreshCookie = generateCookie(refreshToken);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(responseDto);
    }


    /**
     * Refreshes the access token.
     *
     * @param token The refresh token.
     * @return A response entity containing the new access token.
     */
    @PostMapping("refresh-token")
    public ResponseEntity<SimpleMessageDTO> refreshToken(
            @CookieValue(name = "refresh-token", required = false) String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token cookie not found");
        }

        try {
            TokenPair tokens = authService.validateRefreshToken(token);
            ResponseCookie refreshCookie = generateCookie(tokens.getRefreshToken());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(new SimpleMessageDTO(tokens.getJwt()));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token");
        }
    }

    /**
     * A protected endpoint for testing authentication.
     *
     * @param user The authenticated user.
     * @return The user's email.
     */
    @GetMapping("protected")
    public String protec(@AuthenticationPrincipal User user) {
        return user.getEmail();
    }

    /**
     * Generates a refresh token cookie.
     *
     * @param refreshToken The refresh token.
     * @return The response cookie.
     */
    private ResponseCookie generateCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from("refresh-token", refreshToken)
                .httpOnly(true)
//                .path("/api/refresh-token")
                .path("/api")
                .maxAge(30 * 24 * 60 * 60); // 30 days

        if (secureCookies) {
            // for production (HTTPS)
            builder.secure(true)
                    .sameSite(SameSiteCookies.NONE.toString());
        } else {
            // for development (HTTP)
            builder.secure(false)
                    .sameSite(SameSiteCookies.LAX.toString());
        }

        return builder.build();
    }

}
