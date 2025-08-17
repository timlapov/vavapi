package art.lapov.vavapi.service;

import art.lapov.vavapi.enums.ReservationStatus;
import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for automatic reservation status management
 * Runs periodically to check and update reservation statuses
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationAutoCompletionService {

    private final ReservationRepository reservationRepository;
    private final MailService mailService;

    /**
     * Main scheduled task - runs every 5 minutes
     * Checks for PAID reservations that have ended and marks them as COMPLETED
     * This is the PRIMARY mechanism for automatic completion
     */
    @Scheduled(fixedDelay = 900000) // Every 15 minutes (900,000 ms)
    @Transactional
    public void completeEndedReservations() {
        log.debug("Running automatic reservation completion check...");

        LocalDateTime now = LocalDateTime.now();

        // Find all PAID reservations where end time has passed
        List<Reservation> reservationsToComplete = reservationRepository
                .findReservationsToComplete(now);

        if (!reservationsToComplete.isEmpty()) {
            log.info("Found {} reservations to automatically complete", reservationsToComplete.size());

            int successCount = 0;
            for (Reservation reservation : reservationsToComplete) {
                try {
                    // Change status from PAID to COMPLETED
                    reservation.setStatus(ReservationStatus.COMPLETED);
                    reservationRepository.save(reservation);

                    successCount++;
                    log.info("Automatically completed reservation: {} (Client: {}, Station: {}, End: {})",
                            reservation.getId(),
                            reservation.getClient().getEmail(),
                            reservation.getStation().getLocation().getName(),
                            reservation.getEndDate());

                    mailService.sendReservationCompleted(reservation.getClient(), reservation);

                } catch (Exception e) {
                    log.error("Error completing reservation {}: {}",
                            reservation.getId(), e.getMessage(), e);
                }
            }

            log.info("Successfully completed {}/{} reservations",
                    successCount, reservationsToComplete.size());
        }
    }

}