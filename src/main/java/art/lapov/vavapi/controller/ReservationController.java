package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.AvailabilityResponseDTO;
import art.lapov.vavapi.dto.PaymentDetailsDTO;
import art.lapov.vavapi.dto.RejectReasonDTO;
import art.lapov.vavapi.dto.ReservationCreateDTO;
import art.lapov.vavapi.dto.ReservationDTO;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.ReservationService;
import art.lapov.vavapi.service.receipt.ReceiptFacade;
import art.lapov.vavapi.service.report.XlsxGenerationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final XlsxGenerationService xlsxGenerationService;
    private final ReservationService reservationService;
    private final ReceiptFacade receiptFacade;

    /**
     * Create a new reservation
     * Client creates a reservation for a station
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO createReservation(
            @RequestBody @Valid ReservationCreateDTO dto,
            @AuthenticationPrincipal User client) {
        return reservationService.createReservation(dto, client);
    }

    /**
     * Check station availability
     * Public endpoint - anyone can check if station is available
     */
    @GetMapping("/check-availability")
    public AvailabilityResponseDTO checkAvailability(
            @RequestParam String stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        boolean available = reservationService.isStationAvailable(stationId, startDate, endDate);
        return new AvailabilityResponseDTO(available, stationId, startDate, endDate);
    }

    /**
     * Get user's reservations
     * Returns paginated list of all reservations for authenticated user
     */
    @GetMapping("/my")
    public Page<ReservationDTO> getMyReservations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (size > 30) size = 30;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationService.getUserReservations(user, pageable);
    }

    /**
     * Get specific reservation details
     * User can only view their own reservation or if they're the station owner
     */
    @GetMapping("/{id}")
    public ReservationDTO getReservation(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        return reservationService.getReservationById(id, user);
    }

    /**
     * Get station's reservations (for station owner)
     * Owner can see all reservations for their station
     */
    @GetMapping("/station/{stationId}")
    public Page<ReservationDTO> getStationReservations(
            @PathVariable String stationId,
            @AuthenticationPrincipal User owner,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationService.getStationReservations(stationId, owner, pageable);
    }

    /**
     * Accept a reservation (station owner only)
     */
    @PutMapping("/{id}/accept")
    public ReservationDTO acceptReservation(
            @PathVariable String id,
            @AuthenticationPrincipal User owner) {
        return reservationService.acceptReservation(id, owner);
    }

    /**
     * Reject a reservation (station owner only)
     */
    @PutMapping("/{id}/reject")
    public ReservationDTO rejectReservation(
            @PathVariable String id,
            @AuthenticationPrincipal User owner,
            @RequestBody(required = false) RejectReasonDTO reasonDto) {

        String reason = reasonDto != null ? reasonDto.getReason() : "No reason provided";
        return reservationService.rejectReservation(id, owner, reason);
    }

    /**
     * Cancel a reservation (client only)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(
            @PathVariable String id,
            @AuthenticationPrincipal User client) {
        reservationService.cancelReservation(id, client);
    }

    /**
     * Complete a reservation manually (admin or system)
     * This could be triggered by a scheduled job or admin action
     */
    @PutMapping("/{id}/complete")
    public Map<String, String> completeReservation(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        reservationService.completeReservation(id);
        return Map.of("message", "Reservation completed successfully", "reservationId", id);
    }

    /**
     * Get reservations pending approval (for station owners)
     * Returns all reservations waiting for owner's decision
     */
    @GetMapping("/pending-approval")
    public Page<ReservationDTO> getPendingApprovals(
            @AuthenticationPrincipal User owner,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationService.getPendingApprovals(owner, pageable);
    }

    /**
     * Get upcoming reservations for user
     */
    @GetMapping("/upcoming")
    public Page<ReservationDTO> getUpcomingReservations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationService.getUpcomingReservations(user, pageable);
    }

    /**
     * Get past reservations for user (history)
     */
    @GetMapping("/history")
    public Page<ReservationDTO> getPastReservations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        if (size > 50) size = 50;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);

        return reservationService.getPastReservations(user, pageable);
    }

    /**
     * Process payment for an accepted reservation
     * Client pays after owner accepts the reservation
     */
    @PostMapping("/{id}/pay")
    public ReservationDTO processPayment(
            @PathVariable String id,
            @AuthenticationPrincipal User client,
            @RequestBody @Valid PaymentDetailsDTO paymentDetails) {
        return reservationService.processPayment(id, client, paymentDetails);
    }

    /**
     * The client receives the reservation receipt in PDF format
     */
    @GetMapping("/{id}/receipt.pdf")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String id, @AuthenticationPrincipal User user) {
        byte[] pdf = receiptFacade.buildReceiptPdf(id, user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf")
                .body(pdf);
    }

    /**
     * Export completed reservations as CLIENT to Excel
     * Downloads all past reservations where the user was the client
     */
    @GetMapping("/export/client-reservations.xlsx")
    public ResponseEntity<byte[]> exportClientReservations(@AuthenticationPrincipal User client) {
        try {
            byte[] excelFile = xlsxGenerationService.generateClientReservationsReport(client);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("my-reservations-" + LocalDate.now() + ".xlsx")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating Excel report");
        }
    }

    /**
     * Export completed reservations as OWNER to Excel
     * Downloads all past reservations for stations owned by the user
     */
    @GetMapping("/export/owner-reservations.xlsx")
    public ResponseEntity<byte[]> exportOwnerReservations(@AuthenticationPrincipal User owner) {
        try {
            byte[] excelFile = xlsxGenerationService.generateOwnerReservationsReport(owner);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("reservations-my-stations-" + LocalDate.now() + ".xlsx")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error generating Excel report");
        }
    }

}