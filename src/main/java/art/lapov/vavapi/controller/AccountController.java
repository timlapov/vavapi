package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.UserCreateDTO;
import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.dto.UserUpdatePasswordDTO;
import art.lapov.vavapi.mapper.UserMapper;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/account")
class AccountController {
    private AccountService accountService;
    private UserMapper userMapper;

    private final String loginUrl = "http://localhost:4200/login";

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO register(@RequestBody @Valid UserCreateDTO dto) {
        User user = accountService.register(userMapper.map(dto));
        return userMapper.map(user);
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Void> activate(@PathVariable String token) {
        accountService.activateUser(token);
        URI redirectUri = URI.create(loginUrl + "?activated=true");
        // 302 FOUND + Location
        return ResponseEntity.status(302)
                .location(redirectUri)
                .build();
    }

    @PostMapping("/password/{email}")
    public ResponseEntity<Void> resetPassword(@PathVariable String email) {
        accountService.resetPassword(email);
        URI redirectUri = URI.create(loginUrl + "?password_reset=true");
        return ResponseEntity.status(200)
                .location(redirectUri)
                .build();
    }

    @PatchMapping("/password")
    public String updatePassword(@AuthenticationPrincipal User user, @RequestBody UserUpdatePasswordDTO dto) {
        accountService.updatePassword(user, dto.getNewPassword());
        return "Password updated";
    }

}
