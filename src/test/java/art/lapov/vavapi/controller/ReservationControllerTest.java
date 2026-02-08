package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.*;
import art.lapov.vavapi.enums.ReservationStatus;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.ReservationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    private User mockUser;
    private ReservationDTO sampleReservationDTO;
    private ReservationCreateDTO createDTO;
    private PaymentDetailsDTO paymentDetailsDTO;

    @BeforeEach
    void setUp() {
        // Initialize mock user
        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setRole("ROLE_USER");
        mockUser.setValidated(true);

        // Initialize sample DTOs
        sampleReservationDTO = new ReservationDTO(
                "res-123",
                ReservationStatus.CREATED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                5000, // 50 euros
                LocalDateTime.now(),
                null,
                null,
                createStationShortDTO(),
                createUserShortDTO(),
                null,
                null,
                null
        );

        createDTO = new ReservationCreateDTO(
                "station-456",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        paymentDetailsDTO = new PaymentDetailsDTO(
                "4111111111111111",
                "12/25",
                "123",
                "John Doe"
        );
    }

    // ============= CREATE RESERVATION TESTS =============

    @Test
    @WithMockUser
    void createReservation_Success() throws Exception {
        when(reservationService.createReservation(any(ReservationCreateDTO.class), any(User.class)))
                .thenReturn(sampleReservationDTO);

        mockMvc.perform(post("/api/reservations")
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("res-123"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalCostInCents").value(5000));

        verify(reservationService, times(1))
                .createReservation(any(ReservationCreateDTO.class), eq(mockUser));
    }

    @Test
    @WithMockUser
    void createReservation_InvalidDates_BadRequest() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        ReservationCreateDTO invalidDTO = new ReservationCreateDTO(
                "station-456",
                now.plusDays(2), // start after end
                now.plusDays(1)
        );

        when(reservationService.createReservation(any(ReservationCreateDTO.class), any(User.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be before endDate"));

        mockMvc.perform(post("/api/reservations")
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReservation_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());;
    }

    // ============= CHECK AVAILABILITY TESTS =============

    @Test
    void checkAvailability_Available() throws Exception {
        when(reservationService.isStationAvailable(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        mockMvc.perform(get("/api/reservations/check-availability")
                        .param("stationId", "station-123")
                        .param("startDate", "2024-12-01T10:00:00")
                        .param("endDate", "2024-12-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.stationId").value("station-123"));
    }

    @Test
    void checkAvailability_NotAvailable() throws Exception {
        when(reservationService.isStationAvailable(anyString(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        mockMvc.perform(get("/api/reservations/check-availability")
                        .param("stationId", "station-123")
                        .param("startDate", "2024-12-01T10:00:00")
                        .param("endDate", "2024-12-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    // ============= GET MY RESERVATIONS TESTS =============

    @Test
    @WithMockUser
    void getMyReservations_Success() throws Exception {
        Page<ReservationDTO> page = new PageImpl<>(
                Arrays.asList(sampleReservationDTO),
                PageRequest.of(0, 10),
                1
        );

        when(reservationService.getUserReservations(any(User.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reservations/my")
                        .with(user(mockUser))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value("res-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ============= GET SPECIFIC RESERVATION TESTS =============

    @Test
    @WithMockUser
    void getReservation_Success() throws Exception {
        when(reservationService.getReservationById(anyString(), any(User.class)))
                .thenReturn(sampleReservationDTO);

        mockMvc.perform(get("/api/reservations/res-123")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("res-123"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    // ============= ACCEPT RESERVATION TESTS =============

    @Test
    @WithMockUser
    void acceptReservation_Success() throws Exception {
        ReservationDTO acceptedDTO = new ReservationDTO(
                "res-123",
                ReservationStatus.ACCEPTED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                5000,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                createStationShortDTO(),
                createUserShortDTO(),
                null,
                null,
                null
        );

        when(reservationService.acceptReservation(anyString(), any(User.class)))
                .thenReturn(acceptedDTO);

        mockMvc.perform(put("/api/reservations/res-123/accept")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.acceptedAt").exists());
    }

    // ============= REJECT RESERVATION TESTS =============

    @Test
    @WithMockUser
    void rejectReservation_Success() throws Exception {
        ReservationDTO rejectedDTO = new ReservationDTO(
                "res-123",
                ReservationStatus.REJECTED,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                5000,
                LocalDateTime.now(),
                null,
                null,
                createStationShortDTO(),
                createUserShortDTO(),
                null,
                null,
                null
        );

        when(reservationService.rejectReservation(anyString(), any(User.class)))
                .thenReturn(rejectedDTO);

        mockMvc.perform(put("/api/reservations/res-123/reject")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(reservationService).rejectReservation(eq("res-123"), eq(mockUser));
    }

    // ============= CANCEL RESERVATION TESTS =============

    @Test
    @WithMockUser
    void cancelReservation_Success() throws Exception {
        doNothing().when(reservationService).cancelReservation(anyString(), any(User.class));

        mockMvc.perform(delete("/api/reservations/res-123")
                        .with(user(mockUser)))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).cancelReservation(eq("res-123"), eq(mockUser));
    }

    // ============= COMPLETE RESERVATION TESTS =============

    @Test
    @WithMockUser
    void completeReservation_Success() throws Exception {
        doNothing().when(reservationService).completeReservation(anyString());

        mockMvc.perform(put("/api/reservations/res-123/complete")
                        .with(user(mockUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reservation completed successfully"))
                .andExpect(jsonPath("$.reservationId").value("res-123"));
    }

    // ============= PROCESS PAYMENT TESTS =============

    @Test
    @WithMockUser
    void processPayment_Success() throws Exception {
        ReservationDTO paidDTO = new ReservationDTO(
                "res-123",
                ReservationStatus.PAID,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                5000,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                createStationShortDTO(),
                createUserShortDTO(),
                createPaymentDTO(),
                null,
                null
        );

        when(reservationService.processPayment(anyString(), any(User.class), any(PaymentDetailsDTO.class)))
                .thenReturn(paidDTO);

        mockMvc.perform(post("/api/reservations/res-123/pay")
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDetailsDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAt").exists())
                .andExpect(jsonPath("$.payment").exists());
    }

    @Test
    @WithMockUser
    void processPayment_InvalidCardDetails() throws Exception {
        PaymentDetailsDTO invalidPayment = new PaymentDetailsDTO(
                "invalid",
                "13/25", // Invalid month
                "12", // Invalid CVV length
                "John Doe"
        );

        mockMvc.perform(post("/api/reservations/res-123/pay")
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayment)))
                .andExpect(status().isBadRequest());
    }

    // ============= PAGINATION TESTS =============

    @Test
    @WithMockUser
    void getUpcomingReservations_WithPagination() throws Exception {
        Page<ReservationDTO> page = new PageImpl<>(
                Arrays.asList(sampleReservationDTO),
                PageRequest.of(0, 10),
                1
        );

        when(reservationService.getUpcomingReservations(any(User.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reservations/upcoming")
                        .with(user(mockUser))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @WithMockUser
    void getPastReservations_EmptyResult() throws Exception {
        Page<ReservationDTO> emptyPage = new PageImpl<>(
                Collections.emptyList(),
                PageRequest.of(0, 20),
                0
        );

        when(reservationService.getPastReservations(any(User.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/reservations/history")
                        .with(user(mockUser))
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ============= STATION RESERVATIONS TESTS =============

    @Test
    @WithMockUser
    void getStationReservations_AsOwner() throws Exception {
        Page<ReservationDTO> page = new PageImpl<>(
                Arrays.asList(sampleReservationDTO),
                PageRequest.of(0, 20),
                1
        );

        when(reservationService.getStationReservations(anyString(), any(User.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reservations/station/station-123")
                        .with(user(mockUser))
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ============= PENDING APPROVALS TESTS =============

    @Test
    @WithMockUser
    void getPendingApprovals_Success() throws Exception {
        ReservationDTO pendingDTO = new ReservationDTO(
                "res-456",
                ReservationStatus.CREATED,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                3000,
                LocalDateTime.now(),
                null,
                null,
                createStationShortDTO(),
                createUserShortDTO(),
                null,
                null,
                null
        );

        Page<ReservationDTO> page = new PageImpl<>(
                Arrays.asList(pendingDTO),
                PageRequest.of(0, 10),
                1
        );

        when(reservationService.getPendingApprovals(any(User.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/reservations/pending-approval")
                        .with(user(mockUser))
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("CREATED"));
    }

    // ============= CROSSING MIDNIGHT TESTS =============

    @Test
    @WithMockUser
    void createReservation_CrossesMidnight_Success_CorrectTotalCost() throws Exception {
        // Given: 22:00 → 02:00
        LocalDateTime start = LocalDateTime.of(2026, 6, 15, 22, 0);
        LocalDateTime end   = LocalDateTime.of(2026, 6, 16, 2, 0);

        // Допустим, тарифы дают суммарно 10€ = 1000 центов
        ReservationDTO midnightDTO = new ReservationDTO(
                "res-789",
                ReservationStatus.CREATED,
                start,
                end,
                1000, // 10 €
                LocalDateTime.now(),
                null,
                null,
                createStationShortDTO(),
                createUserShortDTO(),
                null,
                null,
                null
        );

        when(reservationService.createReservation(any(ReservationCreateDTO.class), any(User.class)))
                .thenAnswer(inv -> {
                    ReservationCreateDTO req = inv.getArgument(0, ReservationCreateDTO.class);
                    assertEquals(start, req.getStartDate());
                    assertEquals(end,   req.getEndDate());
                    return midnightDTO;
                });

        ReservationCreateDTO payload = new ReservationCreateDTO("station-456", start, end);

        // When / Then
        mockMvc.perform(post("/api/reservations")
                        .with(user(mockUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("res-789"))
                .andExpect(jsonPath("$.totalCostInCents").value(1000));
    }

    // ============= HELPER METHODS =============

    private StationShortDTO createStationShortDTO() {
        return new StationShortDTO(
                "station-123",
                22000L,
                art.lapov.vavapi.enums.ConnectorType.TYPE2S,
                "Station description",
                createLocationShortDTO()
        );
    }

    private LocationShortDTO createLocationShortDTO() {
        return new LocationShortDTO(
                "loc-123",
                "Test Location",
                "123 Test Street",
                "Paris",
                75001,
                48.8566,
                2.3522
        );
    }

    private UserShortDTO createUserShortDTO() {
        return new UserShortDTO(
                "user-123",
                "John",
                "Doe",
                null
        );
    }

    private PaymentDTO createPaymentDTO() {
        return new PaymentDTO(
                "pay-123",
                5000,
                LocalDateTime.now(),
                "TXN_123456",
                null,
                null
        );
    }
}