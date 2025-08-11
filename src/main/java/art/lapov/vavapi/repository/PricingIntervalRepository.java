package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.PricingInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PricingIntervalRepository extends JpaRepository<PricingInterval, String> {
    /**
     * Find all station pricing intervals sorted by start time
     */
    List<PricingInterval> findByStationIdOrderByStartHour(String stationId);

    /**
     * Find all tariff intervals of the station
     */
    List<PricingInterval> findByStationId(String stationId);
}