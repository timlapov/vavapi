package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.dto.UserUpdateDTO;
import art.lapov.vavapi.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination
     */
    @GetMapping
    public Page<UserDTO> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);
        return userService.getAllUsers(pageable);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    /**
     * Update user by ID
     */
    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable String id,
                              @RequestBody @Valid UserUpdateDTO dto) {
        return userService.updateUser(id, dto);
    }

    /**
     * Soft delete user
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }

    /**
     * Validate user
     */
    @PatchMapping("/{id}/validated")
    public UserDTO toggleUserStatus(@PathVariable String id) {
        return userService.toggleUserStatus(id);
    }

    /**
     * Reset user password (admin generates new temporary password)
     */
    @PostMapping("/{id}/reset-password")
    public String resetUserPassword(@PathVariable String id) {
        return userService.resetUserPassword(id);
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public Page<UserDTO> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);
        return userService.getUsersByRole(role, pageable);
    }

    /**
     * Search users by email or first name or last name
     */
    @GetMapping("/search")
    public Page<UserDTO> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);
        return userService.searchUsers(query, pageable);
    }
}
