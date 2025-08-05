package art.lapov.vavapi.service;

import art.lapov.vavapi.exception.UserAlreadyExistsException;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.UserRepository;
import art.lapov.vavapi.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        return user;
    }

//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        return userRepository
//                .findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }

    public void activateUser(String token) {
        User user = (User)jwtUtil.validateToken(token);
        user.setValidated(true);
        userRepository.save(user);
    }

//    @Override
//    public void createUser(UserDetails user) {
//        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
//            throw new UserAlreadyExistsException();
//        }
//        String hashed = passwordEncoder.encode(user.getPassword());
//        user.setPassword(hashed);
//        user.setRole("ROLE_USER");
//        user.setValidated(false);
//        userRepository.save(user);
//
//        String token = jwtUtil.generateToken(user, Instant.now().plus(7, ChronoUnit.DAYS));
//        mailService.sendEmailValidation(user, token);
//
//        return user;
//    }


//
//    // Finds a user by email
//    Optional<UserDto> findByEmail(String email);
//
//    // Finds a user by ID
//    Optional<UserDto> findById(String id);
//
//    // Updates user's profile information
//    UserDto updateProfile(String userId, UserProfileData profileData);
//
//    // Changes user's role (e.g., promote to admin)
//    void changeUserRole(String userId, Role newRole);
}
