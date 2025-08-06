package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.dto.UserUpdateDTO;
import art.lapov.vavapi.exception.UserAlreadyExistsException;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.UserRepository;
import art.lapov.vavapi.security.JwtUtil;
import lombok.AllArgsConstructor;
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
    //private Logger logger = LoggerFactory.getLogger(getClass());

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

    public void activateUser(String token) {
        User user = (User)jwtUtil.validateToken(token);
        user.setValidated(true);
        userRepository.save(user);
    }

    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        String token = jwtUtil.generateToken(user, Instant.now().plus(1,ChronoUnit.HOURS));
        mailService.sendResetPassword(user, token);
        //logger.info("User requested password reset : "+user.getEmail());
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        // TODO Check that there are no active stations or pending orders
        user.setDeleted(true);
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
        return userRepository.save(user);
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
