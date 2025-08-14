package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.enums.ReservationStatus;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Find all reservations that might conflict with the given time period
     * Uses the same overlap logic: start1 < end2 AND start2 < end1
     */
    @Query("SELECT r FROM Reservation r WHERE r.station.id = :stationId " +
            "AND r.startDate < :endDate " +
            "AND r.endDate > :startDate " +
            "AND r.status NOT IN ('REJECTED', 'CANCELLED')")
    List<Reservation> findConflictingReservations(
            @Param("stationId") String stationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Check if station has any active reservations in the period
     */
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.station.id = :stationId " +
            "AND r.startDate < :endDate " +
            "AND r.endDate > :startDate " +
            "AND r.status IN ('ACCEPTED', 'PAID', 'COMPLETED')")
    boolean existsActiveReservationInPeriod(
            @Param("stationId") String stationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find user's reservations ordered by date
     */
    Page<Reservation> findByClientOrderByStartDateDesc(User client, Pageable pageable);

    /**
     * Find station's reservations ordered by date
     */
    Page<Reservation> findByStationOrderByStartDateDesc(Station station, Pageable pageable);

    /**
     * Find upcoming reservations for a user
     */
    @Query("SELECT r FROM Reservation r WHERE r.client = :client " +
            "AND r.startDate > :now " +
            "AND r.status IN ('CREATED', 'ACCEPTED', 'PAID') " +
            "ORDER BY r.startDate ASC")
    List<Reservation> findUpcomingReservations(
            @Param("client") User client,
            @Param("now") LocalDateTime now);

    /**
     * Find past reservations for a user
     */
    @Query("SELECT r FROM Reservation r WHERE r.client = :client " +
            "AND r.endDate < :now " +
            "ORDER BY r.endDate DESC")
    Page<Reservation> findPastReservations(
            @Param("client") User client,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * Find reservations that should be automatically completed
     */
    @Query("SELECT r FROM Reservation r WHERE r.endDate < :now " +
            "AND r.status IN ('ACCEPTED', 'PAID')")
    List<Reservation> findReservationsToComplete(@Param("now") LocalDateTime now);

    /**
     * Find reservations pending owner approval
     */
    @Query("SELECT r FROM Reservation r WHERE r.station.location.owner = :owner " +
            "AND r.status = 'CREATED' " +
            "ORDER BY r.createdAt DESC")
    List<Reservation> findPendingApprovalForOwner(@Param("owner") User owner);

    /**
     * Find reservations pending owner approval with pagination
     */
    @Query("SELECT r FROM Reservation r WHERE r.station.location.owner = :owner " +
            "AND r.status = 'CREATED' " +
            "ORDER BY r.createdAt DESC")
    Page<Reservation> findPendingApprovalForOwner(@Param("owner") User owner, Pageable pageable);

    /**
     * Find upcoming reservations for a user with pagination
     */
    @Query("SELECT r FROM Reservation r WHERE r.client = :client " +
            "AND r.startDate > :now " +
            "AND r.status IN ('CREATED', 'ACCEPTED', 'PAID') " +
            "ORDER BY r.startDate ASC")
    Page<Reservation> findUpcomingReservations(
            @Param("client") User client,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // Single SQL: checks both client and station owner in WHERE
    @Query("""
        SELECT r FROM Reservation r
          JOIN r.station s
         WHERE r.id = :id
           AND r.client.id = :userId
        """)
    Optional<Reservation> findVisibleToUser(String id, String userId);
}