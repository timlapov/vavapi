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


}