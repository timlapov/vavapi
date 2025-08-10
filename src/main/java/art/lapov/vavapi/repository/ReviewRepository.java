package art.lapov.vavapi.repository;

import art.lapov.vavapi.model.Review;
import art.lapov.vavapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    /**
     * Find a review by reservation
     */
    Optional<Review> findByReservationId(String reservationId);

    /**
     * Find all reviews by the author
     */
    List<Review> findByAuthor(User author);

    /**
     * Find all reviews for a specific station
     */
    @Query("SELECT r FROM Review r WHERE r.reservation.station.id = :stationId")
    List<Review> findByReservationStationId(@Param("stationId") String stationId);

    /**
     * Find all reviews for a specific station + PAGEABLE
     */
    @Query("SELECT r FROM Review r WHERE r.reservation.station.id = :stationId")
    Page<Review> findByReservationStationId(@Param("stationId") String stationId, Pageable pageable);

    /**
     * Find all station reviews for a specific owner
     */
    @Query("SELECT r FROM Review r WHERE r.reservation.station.location.owner.id = :ownerId")
    List<Review> findByStationOwner(@Param("ownerId") String ownerId);

    /**
     * Find all station reviews for a specific owner + PAGEABLE
     */
    @Query("SELECT r FROM Review r WHERE r.reservation.station.location.owner.id = :ownerId")
    Page<Review> findByStationOwner(@Param("ownerId") String ownerId, Pageable pageable);

    boolean existsByReservationId(String reservationId);

    /**
     * Get the average rating of the station
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.reservation.station.id = :stationId")
    Double getAverageRatingByStationId(@Param("stationId") String stationId);

    /**
     * Get the average rating of the owner
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.reservation.station.location.owner.id = :ownerId")
    Double getAverageRatingByOwnerId(@Param("ownerId") String ownerId);
}
