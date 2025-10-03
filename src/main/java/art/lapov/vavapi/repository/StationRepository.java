package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StationRepository extends JpaRepository<Station, String> {

    @Query("SELECT s FROM Station s WHERE s.deleted = false OR s.deleted IS NULL")
    Page<Station> findAll(Pageable pageable);

    @Query("SELECT s FROM Station s WHERE s.location.id = :locationId AND s.enabled = true AND (s.deleted = false OR s.deleted IS NULL)")
    List<Station> findByLocationIdAndEnabledTrue(@Param("locationId") String locationId);

    /**
     * Check if user has any enabled stations
     */
    @Query("SELECT COUNT(s) > 0 FROM Station s " +
            "WHERE s.location.owner.id = :userId " +
            "AND s.enabled = true " +
            "AND (s.deleted = false OR s.deleted IS NULL)")
    boolean hasActiveStations(@Param("userId") String userId);

    /**
     * Check if location has any active stations
     */
    @Query("SELECT COUNT(s) > 0 FROM Station s " +
            "WHERE s.location.id = :locationId " +
            "AND s.enabled = true " +
            "AND (s.deleted = false OR s.deleted IS NULL)")
    boolean existsActiveStationsByLocationId(@Param("locationId") String locationId);

    /**
     * Find available stations in a location for a specific time period
     * A station is available if it's enabled, not deleted, and has no active reservations in the period
     */
    @Query("SELECT s FROM Station s " +
            "WHERE s.location.id = :locationId " +
            "AND s.enabled = true " +
            "AND (s.deleted = false OR s.deleted IS NULL) " +
            "AND NOT EXISTS (" +
            "  SELECT r FROM Reservation r " +
            "  WHERE r.station.id = s.id " +
            "  AND r.startDate < :endDate " +
            "  AND r.endDate > :startDate " +
            "  AND r.status IN ('ACCEPTED', 'PAID', 'COMPLETED')" +
            ")")
    List<Station> findAvailableStationsByLocationAndPeriod(
            @Param("locationId") String locationId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

}