package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.LoginCredentialsDTO;
import art.lapov.vavapi.dto.LoginResponseDTO;
import art.lapov.vavapi.mapper.UserMapper;
import art.lapov.vavapi.model.RefreshToken;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.RefreshTokenRepository;
import art.lapov.vavapi.repository.UserRepository;
import art.lapov.vavapi.security.JwtUtil;
import art.lapov.vavapi.security.TokenPair;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class AuthService {
    private AuthenticationManager authManager;
    private JwtUtil jwtUtil;
    private UserMapper mapper;
    private RefreshTokenRepository tokenRepository;
    private UserRepository userRepo;

    public LoginResponseDTO login(LoginCredentialsDTO credentials) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.getEmail(),
                        credentials.getPassword()));
        User user = (User) authentication.getPrincipal();

        String token = jwtUtil.generateToken(user);
        return new LoginResponseDTO(token, mapper.map(user));
    }

    public String generateRefreshToken(String idUser) {
        RefreshToken refreshToken = new RefreshToken();
        User user = userRepo.findById(idUser).orElseThrow();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(30, ChronoUnit.DAYS));
        tokenRepository.save(refreshToken);
        return refreshToken.getId();
    }

    public TokenPair validateRefreshToken(String token) {
        RefreshToken refreshToken = tokenRepository.findById(token).orElseThrow();
        if (refreshToken.isExpired()) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();
        tokenRepository.delete(refreshToken);
        String newToken = generateRefreshToken(user.getId());
        String jwt = jwtUtil.generateToken(user);
        return new TokenPair(jwt, newToken);

    }

    @Transactional
    @Scheduled(fixedDelay = 24, timeUnit = TimeUnit.HOURS)
    void cleanExpiredTokens() {
        tokenRepository.deleteExpired();
    }

}
