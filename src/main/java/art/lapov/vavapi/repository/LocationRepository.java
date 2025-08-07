package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    @Query(value = """
        SELECT l.*, (
        6371 * 2 * ASIN(
            SQRT(
            POWER(SIN(RADIANS(l.latitude - :lat0) / 2), 2) +
            COS(RADIANS(:lat0)) * COS(RADIANS(l.latitude)) *
            POWER(SIN(RADIANS(l.longitude - :lon0) / 2), 2)
            )
        )
        ) AS distance_km
        FROM location l
        WHERE l.latitude BETWEEN :lat0 - (:R / 110.574) AND :lat0 + (:R / 110.574)
        AND l.longitude BETWEEN :lon0 - (:R / (111.320 * COS(RADIANS(:lat0))))
            AND :lon0 + (:R / (111.320 * COS(RADIANS(:lat0))))
        AND (l.deleted IS NULL OR l.deleted = false)
        HAVING distance_km <= :R
        ORDER BY distance_km
        """, nativeQuery = true)
    List<Location> findWithinRadius(
            @Param("lat0") double lat0,
            @Param("lon0") double lon0,
            @Param("R") double radiusKm);

    Page<Location> findAll(Pageable pageable);

}