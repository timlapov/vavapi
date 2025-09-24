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

}