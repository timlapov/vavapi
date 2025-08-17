package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.dto.UserUpdateDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.exception.UserHasActiveReservationException;
import art.lapov.vavapi.mapper.UserMapper;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.StationRepository;
import art.lapov.vavapi.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /**
     * Get all users with pagination
     */
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::map);
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.map(user);
    }

    /**
     * Update user by ID (admin only)
     */
    @Transactional
    public UserDTO updateUser(String id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userMapper.update(dto, user);
        User updatedUser = userRepository.save(user);
        return userMapper.map(updatedUser);
    }

    /**
     * Soft delete user
     */
    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Same checks as in AccountService
        if (userRepository.hasActiveReservations(user.getId())) {
            throw new UserHasActiveReservationException("User has active reservations as client");
        }

        if (userRepository.hasActiveReservationsAsOwner(user.getId())) {
            throw new UserHasActiveReservationException("User has active reservations on their stations");
        }

        if (stationRepository.hasActiveStations(user.getId())) {
            throw new UserHasActiveReservationException("User has active stations. Please disable all stations first");
        }

        user.setDeleted(true);
        user.setValidated(false); // Prevent login
        userRepository.save(user);
    }


    /**
     * Toggle user validation status
     */
    @Transactional
    public UserDTO toggleUserStatus(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setValidated(!user.getValidated());
        User updatedUser = userRepository.save(user);
        return userMapper.map(updatedUser);
    }

    /**
     * Reset user password (admin generates new temporary password)
     */
    @Transactional
    public String resetUserPassword(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Generate temporary password
        String newPassword = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send email with new password
        mailService.sendResetPasswordByAdmin(user, newPassword);

        return "Password reset successfully. New password sent to user's email.";
    }

    /**
     * Get users by role
     */
    public Page<UserDTO> getUsersByRole(String role, Pageable pageable) {
        // Ensure role has ROLE_ prefix
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return userRepository.findByRole(normalizedRole, pageable)
                .map(userMapper::map);
    }

    /**
     * Search users by email, first name, or last name
     */
    public Page<UserDTO> searchUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query.toLowerCase(), pageable)
                .map(userMapper::map);
    }

    /**
     * Generate a temporary password
     */
    private String generateTemporaryPassword() {
        // Generate a random 8-character password
        return UUID.randomUUID().toString().substring(0, 8);
    }

}