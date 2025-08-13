package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.CostCalculationDTO;
import art.lapov.vavapi.dto.PaymentDetailsDTO;
import art.lapov.vavapi.dto.ReservationCreateDTO;
import art.lapov.vavapi.dto.ReservationDTO;
import art.lapov.vavapi.enums.ReservationStatus;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.mapper.ReservationMapper;
import art.lapov.vavapi.model.*;
import art.lapov.vavapi.repository.PaymentRepository;
import art.lapov.vavapi.repository.ReservationRepository;
import art.lapov.vavapi.repository.StationRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final StationRepository stationRepository;
    private final PaymentRepository paymentRepository;
    private final PricingIntervalService pricingIntervalService;
    private final ReservationMapper reservationMapper;
    private final MailService mailService;

    /**
     * Create a new reservation request (NOT PAID YET)
     */
    @Transactional
    public ReservationDTO createReservation(ReservationCreateDTO dto, User client) {
        // 1. Validate station exists
        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Station not found"));

        // 2. Validate station is enabled
        if (Boolean.FALSE.equals(station.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Station is not available for reservations");
        }

        // 3. Validate dates
        validateReservationDates(dto.getStartDate(), dto.getEndDate());

        // 4. Check station availability (NO OTHER RESERVATIONS)
        if (!isStationAvailable(dto.getStationId(), dto.getStartDate(), dto.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Station is already booked for this time period");
        }

        // 5. Check pricing intervals exist for this time
        if (!pricingIntervalService.isStationAvailable(dto.getStationId(),
                dto.getStartDate(), dto.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No pricing defined for selected time period");
        }

        // 6. Calculate cost (for information only, payment will be after approval)
        CostCalculationDTO costCalculation = pricingIntervalService.calculateCost(
                dto.getStationId(), dto.getStartDate(), dto.getEndDate());

        // 7. Create reservation WITHOUT payment
        Reservation reservation = new Reservation();
        reservation.setStation(station);
        reservation.setClient(client);
        reservation.setStartDate(dto.getStartDate());
        reservation.setEndDate(dto.getEndDate());
        reservation.setTotalCostInCents(costCalculation.getTotalCostInCents());
        reservation.setStatus(ReservationStatus.CREATED);
        // NO PAYMENT YET - payment will be created after owner approval

        Reservation saved = reservationRepository.save(reservation);

        // 8. Notify owner about new reservation request
        mailService.sendNewReservationRequest(station.getLocation().getOwner(), saved);

        // 9. Send confirmation to client that request was received
        mailService.sendReservationRequestReceived(client, saved);

        return reservationMapper.map(saved);
    }

    /**
     * Accept a reservation (by station owner)
     * After acceptance, client needs to pay
     */
    @Transactional
    public ReservationDTO acceptReservation(String reservationId, User owner) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Verify owner
        if (!reservation.getStation().getLocation().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only station owner can accept reservations");
        }

        // Check status
        if (reservation.getStatus() != ReservationStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reservation cannot be accepted in current status: " + reservation.getStatus());
        }

        // Double-check availability (in case of concurrent reservations)
        if (!isStationAvailableExcluding(reservation.getStation().getId(),
                reservation.getStartDate(), reservation.getEndDate(), reservationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Station is no longer available for this time period");
        }

        reservation.setStatus(ReservationStatus.ACCEPTED);
        reservation.setAcceptedAt(LocalDateTime.now());

        Reservation updated = reservationRepository.save(reservation);

        // Notify client to proceed with payment
        mailService.sendReservationAcceptedPleasePayRequest(reservation.getClient(), updated);

        return reservationMapper.map(updated);
    }

    /**
     * Process payment for accepted reservation (by client)
     */
    @Transactional
    public ReservationDTO processPayment(String reservationId, User client, PaymentDetailsDTO paymentDetails) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Verify client
        if (!reservation.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only pay for your own reservations");
        }

        // Check status - must be ACCEPTED
        if (reservation.getStatus() != ReservationStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reservation must be accepted before payment. Current status: " + reservation.getStatus());
        }

        // Check if not too late (e.g., reservation start time hasn't passed)
        if (LocalDateTime.now().isAfter(reservation.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot pay for reservation that has already started");
        }

        // Process payment
        Payment payment = processPaymentGateway(paymentDetails, reservation.getTotalCostInCents());

        // Update reservation
        reservation.setPayment(payment);
        reservation.setStatus(ReservationStatus.PAID);
        reservation.setPaidAt(LocalDateTime.now());

        Reservation updated = reservationRepository.save(reservation);

        return reservationMapper.map(updated);
    }

    /**
     * Get user's reservations
     */
    public Page<ReservationDTO> getUserReservations(User user, Pageable pageable) {
        return reservationRepository.findByClientOrderByStartDateDesc(user, pageable)
                .map(reservationMapper::map);
    }

    public ReservationDTO getReservationById(String reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
        if (!reservation.getClient().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view your own reservations");
        }
        return reservationMapper.map(reservation);
    }

    /**
     * Get station's reservations (for owner)
     */
    public Page<ReservationDTO> getStationReservations(String stationId, User owner, Pageable pageable) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found"));

        // Verify owner
        if (!station.getLocation().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only view reservations for your own stations");
        }

        return reservationRepository.findByStationOrderByStartDateDesc(station, pageable)
                .map(reservationMapper::map);
    }

    /**
     * Reject a reservation (by station owner)
     * No refund needed as payment hasn't been made yet
     */
    @Transactional
    public ReservationDTO rejectReservation(String reservationId, User owner, String reason) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Verify owner
        if (!reservation.getStation().getLocation().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only station owner can reject reservations");
        }

        // Check status - can only reject CREATED (not yet accepted)
        if (reservation.getStatus() != ReservationStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Can only reject reservations with CREATED status. Current: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.REJECTED);
        // No payment to refund as it wasn't paid yet

        Reservation updated = reservationRepository.save(reservation);

        // Notify client with reason
        mailService.sendReservationRejected(reservation.getClient(), updated, reason);

        return reservationMapper.map(updated);
    }

    /**
     * Cancel a reservation (by client)
     * Different logic based on status
     */
    @Transactional
    public void cancelReservation(String reservationId, User client) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Verify client
        if (!reservation.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only cancel your own reservations");
        }

        // Handle based on current status
        switch (reservation.getStatus()) {
            case CREATED, ACCEPTED:
                // Not paid yet, simple cancellation
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);

                // Notify owner
                mailService.sendReservationCancelled(
                        reservation.getStation().getLocation().getOwner(), reservation);
                break;

            case PAID:
                // Already paid, need to process refund
                LocalDateTime cancellationDeadline = reservation.getStartDate().minusHours(24);
                boolean fullRefund = LocalDateTime.now().isBefore(cancellationDeadline);

                reservation.setStatus(ReservationStatus.CANCELLED);

                if (fullRefund) {
                    processRefund(reservation.getPayment(), "Full refund - cancelled 24h+ before start");
                } else {
                    // Partial refund - 50% for late cancellation
                    processPartialRefund(reservation.getPayment(), 50, "Late cancellation - 50% refund");
                }

                reservationRepository.save(reservation);

                break;

            case COMPLETED, REJECTED, CANCELLED:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cannot cancel reservation with status: " + reservation.getStatus());
        }
    }

    /**
     * Mark reservation as completed (automatic or manual)
     */
    @Transactional
    public void completeReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot complete reservation with status: " + reservation.getStatus());
        }

        // Check if end time has passed
        if (LocalDateTime.now().isBefore(reservation.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot complete reservation before end time");
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
    }

    /**
     * Check if station is available, excluding a specific reservation
     */
    private boolean isStationAvailableExcluding(String stationId, LocalDateTime startTime,
                                                LocalDateTime endTime, String excludeReservationId) {
        List<Reservation> potentialConflicts = reservationRepository.findConflictingReservations(
                stationId, startTime, endTime);

        return potentialConflicts.stream()
                .filter(r -> !r.getId().equals(excludeReservationId)) // Exclude current reservation
                .noneMatch(r -> r.getStatus() == ReservationStatus.ACCEPTED ||
                        r.getStatus() == ReservationStatus.PAID ||
                        r.getStatus() == ReservationStatus.COMPLETED);
    }

    /**
     * Check if station is available for the specified time period
     */
    public boolean isStationAvailable(String stationId, LocalDateTime startTime, LocalDateTime endTime) {
        return isStationAvailableExcluding(stationId, startTime, endTime, null);
    }

    // ============= PRIVATE HELPER METHODS =============

    private void validateReservationDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start and end dates are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start date must be before end date");
        }

        if (startDate.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot make reservations in the past");
        }

        // Minimum reservation duration (1 hour)
        if (startDate.plusHours(1).isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Minimum reservation duration is 1 hour");
        }

        // Maximum reservation duration (7 days for MVP)
        if (startDate.plusDays(7).isBefore(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum reservation duration is 7 days");
        }
    }

    private Payment processPaymentGateway(PaymentDetailsDTO paymentDetails, Integer amountInCents) {
        // TODO: Integrate with real payment gateway (Stripe, PayPal, etc.)
        // For MVP, just simulate payment

        Payment payment = new Payment();
        payment.setAmountInCents(amountInCents);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId("TXN_" + UUID.randomUUID());

        return paymentRepository.save(payment);
    }

    private void processRefund(Payment payment, String reason) {
        // TODO: Call payment gateway API for refund
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundedAmountInCents(payment.getAmountInCents());
        payment.setRefundReason(reason);
        payment.setRefundTransactionId("REFUND_" + UUID.randomUUID());
        paymentRepository.save(payment);
    }

    private void processPartialRefund(Payment payment, Integer percentage, String reason) {
        // TODO: Call payment gateway API for partial refund
        Integer refundAmount = (payment.getAmountInCents() * percentage) / 100;
        payment.setRefundedAmountInCents(refundAmount);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(reason);
        payment.setRefundTransactionId("PARTIAL_REFUND_" + UUID.randomUUID());
        paymentRepository.save(payment);
    }

    public Page<ReservationDTO> getPendingApprovals(User owner, Pageable pageable) {
        return reservationRepository.findPendingApprovalForOwner(owner, pageable)
                .map(reservationMapper::map);
    }

    public Page<ReservationDTO> getUpcomingReservations(User user, Pageable pageable) {
        return reservationRepository.findUpcomingReservations(user, LocalDateTime.now(), pageable)
                .map(reservationMapper::map);
    }

    public Page<ReservationDTO> getPastReservations(User user, Pageable pageable) {
        return reservationRepository.findPastReservations(user, LocalDateTime.now(), pageable)
                .map(reservationMapper::map);
    }
}