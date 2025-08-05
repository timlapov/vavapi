package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.UserCreateDTO;
import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.mapper.UserMapper;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public String activate(@PathVariable String token) {
        accountService.activateUser(token);
        return "redirect:" + loginUrl + "?activated=true";
    }

}
