package art.lapov.vavapi.controller;

import art.lapov.vavapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/")
public class AuthController {
    private final AuthService authService;

//    @PostMapping("login")
//    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginCredentialsDTO credentials) {
//        LoginResponseDTO responseDto = authService.login(credentials);
//        String refreshToken = authService.generateRefreshToken(responseDto.getUser().getId());
//        ResponseCookie refreshCookie = generateCookie(refreshToken);
//        return ResponseEntity
//                .ok()
//                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//                .body(responseDto);
//    }
//
//
//    @PostMapping("refresh-token")
//    public ResponseEntity<String> refreshToken(@CookieValue(name = "refresh-token") String token) {
//        try {
//
//            TokenPair tokens = authService.validateRefreshToken(token);
//            ResponseCookie refreshCookie = generateCookie(tokens.getRefreshToken());
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//                    .body(tokens.getJwt());
//
//        } catch (Exception e) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid refresh token");
//        }
//
//    }

//    @GetMapping("/api/protected")
//    public String protec(@AuthenticationPrincipal User user) {
//        System.out.println("hola");
//        return user.getEmail();
//    }

    private ResponseCookie generateCookie(String refreshToken) {
        return ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(false) // HTTPS TODO
                .sameSite(SameSiteCookies.NONE.toString())
                .path("/api/refresh-token")
                //.maxAge(30 * 24 * 60 * 60)
                .build();
    }
}
