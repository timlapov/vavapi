package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByStatus(ReservationStatus status);
}