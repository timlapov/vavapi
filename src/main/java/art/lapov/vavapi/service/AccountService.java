package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.UserUpdateDTO;
import art.lapov.vavapi.exception.UserAlreadyExistsException;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.UserRepository;
import art.lapov.vavapi.utils.JwtUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class AccountService {
    private UserRepository userRepository;
    private MailService mailService;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
//    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("audit");

    public User register(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        String hashed = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashed);
        user.setRole("ROLE_USER");
        user.setValidated(false);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user, Instant.now().plus(7, ChronoUnit.DAYS));
        mailService.sendEmailValidation(user, token);
        auditLogger.info("New user registered: email={}, userId={}", user.getEmail(), user.getId());

        return user;
    }

    public void activateUser(String token) {
        User user = (User)jwtUtil.validateToken(token);
        auditLogger.info("Attempting to activate user: email={}, userId={}", user.getEmail(), user.getId());
        user.setValidated(true);
        userRepository.save(user);
        auditLogger.info("User activated successfully: email={}, userId={}", user.getEmail(), user.getId());
    }

    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        String token = jwtUtil.generateToken(user, Instant.now().plus(1,ChronoUnit.HOURS));
        mailService.sendResetPassword(user, token);
        auditLogger.info("User requested password reset: email={}", user.getEmail());
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogger.info("User updated password: email={}, userId={}", user.getEmail(), user.getId());
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        // TODO Check that there are no active stations or pending orders
        user.setDeleted(true);
        userRepository.save(user);
        auditLogger.info("User marked as deleted: email={}, userId={}", user.getEmail(), user.getId());
    }

    public User updateUser(UserUpdateDTO userDto) {
        User user = userRepository.findByEmail(userDto.getEmail()).orElseThrow();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setCountry(userDto.getCountry());
        user.setCity(userDto.getCity());
        user.setAddress(userDto.getAddress());
        user.setPostalCode(userDto.getPostalCode());
        User updatedUser = userRepository.save(user);
        auditLogger.info("User profile updated: email={}, userId={}", updatedUser.getEmail(), updatedUser.getId());
        return updatedUser;
    }

    public void updateAvatar(String userId, String fileName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPhotoUrl(fileName);
        userRepository.save(user);
    }


}
