package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.ReviewCreateDTO;
import art.lapov.vavapi.dto.ReviewDTO;
import art.lapov.vavapi.dto.ReviewUpdateDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.mapper.ReviewMapper;
import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.enums.ReservationStatus;
import art.lapov.vavapi.model.Review;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.ReservationRepository;
import art.lapov.vavapi.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewMapper reviewMapper;
    private final RatingService ratingService;

    /**
     * Create a review of the reservation
     */
    public ReviewDTO createReservationReview(String reservationId, ReviewCreateDTO dto, User author) {
        // Check that the reservation exists
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        // Check that the author of the review is a customer of the reservation
        if (!reservation.getClient().getId().equals(author.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only review your own reservations");
        }

        // Check that the reservation is complete
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You can only review completed reservations");
        }

        // Check that no feedback has been left yet
        if (reviewRepository.existsByReservationId(reservationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Review for this reservation already exists");
        }

        Review review = reviewMapper.map(dto);
        review.setAuthor(author);
        review.setReservation(reservation);

        Review savedReview = reviewRepository.save(review);

        // Recalculate station and owner ratings
        ratingService.recalculateRatingsForReview(savedReview);

        return reviewMapper.map(savedReview);
    }

    /**
     * Update Review
     */
    public ReviewDTO update(String id, ReviewUpdateDTO dto, User author) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Checking access rights
        if (!review.getAuthor().getId().equals(author.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only update your own reviews");
        }

        reviewMapper.update(dto, review);
        Review updatedReview = reviewRepository.save(review);

        // Recalculate station and owner ratings
        ratingService.recalculateRatingsForReview(updatedReview);

        return reviewMapper.map(updatedReview);
    }

    /**
     * Delete review
     */
    public void delete(String id, User author) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Checking access rights
        if (!review.getAuthor().getId().equals(author.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete your own reviews");
        }

        reviewRepository.deleteById(id);

        // Пересчитать рейтинги после удаления
        ratingService.recalculateRatingsForReview(review);
    }

    /**
     * Get all user reviews
     */
    public List<ReviewDTO> findByAuthor(User author) {
        return reviewRepository.findByAuthor(author)
                .stream()
                .map(reviewMapper::map)
                .toList();
    }

    /**
     * Get feedback on the station
     */
    public Page<ReviewDTO> findByStationId(String stationId, Pageable pageable) {
        return reviewRepository.findByReservationStationId(stationId, pageable)
                .map(reviewMapper::map);
    }

    /**
     * Get owner reviews (for all its stations) with pagination
     */
    public Page<ReviewDTO> findByOwnerId(String ownerId, Pageable pageable) {
        return reviewRepository.findByStationOwner(ownerId, pageable)
                .map(reviewMapper::map);
    }

    /**
     * Get feedback on the reservation
     */
    public ReviewDTO findByReservationId(String reservationId) {
        return reviewRepository.findByReservationId(reservationId)
                .map(reviewMapper::map)
                .orElse(null);
    }
}
