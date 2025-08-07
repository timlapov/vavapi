package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StationRepository extends JpaRepository<Station, String> {
//  @Query(value = """
//        SELECT s.*, (
//        6371 * 2 * ASIN(
//            SQRT(
//            POWER(SIN(RADIANS(s.latitude - :lat0) / 2), 2) +
//            COS(RADIANS(:lat0)) * COS(RADIANS(s.latitude)) *
//            POWER(SIN(RADIANS(s.longitude - :lon0) / 2), 2)
//            )
//        )
//        ) AS distance_km
//        FROM station s
//        WHERE s.latitude BETWEEN :lat0 - (:R / 110.574) AND :lat0 + (:R / 110.574)
//        AND s.longitude BETWEEN :lon0 - (:R / (111.320 * COS(RADIANS(:lat0))))
//            AND :lon0 + (:R / (111.320 * COS(RADIANS(:lat0))))
//        HAVING distance_km <= :R
//        ORDER BY distance_km
//        """, nativeQuery = true)
//  List<Station> findWithinRadius(
//          @Param("lat0") double lat0,
//          @Param("lon0") double lon0,
//          @Param("R") double radiusKm);

    List<Station> findByLocationIdAndEnabledTrue(String locationId);
}