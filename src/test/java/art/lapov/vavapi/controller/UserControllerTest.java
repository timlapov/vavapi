
package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.UserDTO;
import art.lapov.vavapi.dto.UserUpdateDTO;
import art.lapov.vavapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserDTO sampleUserDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        sampleUserDTO = new UserDTO(
                "user-123",
                "ROLE_USER",
                "john.doe@example.com",
                "John",
                "Doe",
                "John Doe",
                "123456789",
                "123 Main St",
                "City",
                "Country",
                12345,
                null,
                null,
                null,
                true,
                null
        );

        updateDTO = new UserUpdateDTO(
                "user-123",
                "john.doe@example.com",
                "John",
                "Doe",
                "123456789",
                "123 Main St",
                "City",
                "Country",
                12345
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        Page<UserDTO> page = new PageImpl<>(
                Arrays.asList(sampleUserDTO),
                PageRequest.of(0, 20),
                1
        );

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value("user-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success() throws Exception {
        when(userService.getUserById(anyString())).thenReturn(sampleUserDTO);

        mockMvc.perform(get("/api/users/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_Success() throws Exception {
        when(userService.updateUser(anyString(), any(UserUpdateDTO.class))).thenReturn(sampleUserDTO);

        mockMvc.perform(put("/api/users/user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"));

        verify(userService, times(1)).updateUser(anyString(), any(UserUpdateDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/user-123"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toggleUserStatus_Success() throws Exception {
        when(userService.toggleUserStatus(anyString())).thenReturn(sampleUserDTO);

        mockMvc.perform(patch("/api/users/user-123/validated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetUserPassword_Success() throws Exception {
        when(userService.resetUserPassword(anyString())).thenReturn("Password reset successfully. New password sent to user's email.");

        mockMvc.perform(post("/api/users/user-123/reset-password"))
                .andExpect(status().isOk())
                .andExpect(content().string("Password reset successfully. New password sent to user's email."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersByRole_Success() throws Exception {
        Page<UserDTO> page = new PageImpl<>(
                Arrays.asList(sampleUserDTO),
                PageRequest.of(0, 20),
                1
        );

        when(userService.getUsersByRole(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/role/USER")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value("user-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchUsers_Success() throws Exception {
        Page<UserDTO> page = new PageImpl<>(
                Arrays.asList(sampleUserDTO),
                PageRequest.of(0, 20),
                1
        );

        when(userService.searchUsers(anyString(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/users/search")
                        .param("query", "john")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value("user-123"));
    }
}
