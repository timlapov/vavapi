package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.RatingStatsDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.model.Review;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.ReviewRepository;
import art.lapov.vavapi.repository.StationRepository;
import art.lapov.vavapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class RatingService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StationRepository stationRepository;

    /**
     * Recalculate station rating based on reservation reviews
     */
    public void recalculateStationRating(String stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found"));

        // Use the repository method to get the average rating
        Double averageRating = reviewRepository.getAverageRatingByStationId(stationId);

        // Get the number of reviews
        List<Review> stationReviews = reviewRepository.findByReservationStationId(stationId);
        int reviewCount = stationReviews.size();

        if (averageRating == null || reviewCount == 0) {
            station.setAverageRating(0.0);
            station.setTotalReviews(0);
        } else {
            // Round to 2 digits
            averageRating = Math.round(averageRating * 100.0) / 100.0;
            station.setAverageRating(averageRating);
            station.setTotalReviews(reviewCount);
        }

        stationRepository.save(station);
    }

    /**
     * Recalculate the owner's rating based on all of their station reviews
     */
    public void recalculateOwnerRating(String ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use the repository method to get the average rating
        Double averageRating = reviewRepository.getAverageRatingByOwnerId(ownerId);

        // Get the number of reviews
        List<Review> ownerStationReviews = reviewRepository.findByStationOwner(ownerId);
        int reviewCount = ownerStationReviews.size();

        if (averageRating == null || reviewCount == 0) {
            owner.setAverageRating(0.0);
            owner.setTotalReviews(0);
        } else {
            // Round to 2 digits
            averageRating = Math.round(averageRating * 100.0) / 100.0;
            owner.setAverageRating(averageRating);
            owner.setTotalReviews(reviewCount);
        }

        userRepository.save(owner);
    }

    /**
     * Recalculate ratings after changing a review
     */
    public void recalculateRatingsForReview(Review review) {
        if (review.getStation() != null) {
            recalculateStationRating(review.getStation().getId());

            User stationOwner = review.getStationOwner();
            if (stationOwner != null) {
                recalculateOwnerRating(stationOwner.getId());
            }
        }
    }

    /**
     * Get station rating statistics
     */
    public RatingStatsDTO getStationRatingStats(String stationId) {
        List<Review> reviews = reviewRepository.findByReservationStationId(stationId);
        return calculateRatingStats(reviews);
    }

    /**
     * Get the owner's rating statistics (for all his stations)
     */
    public RatingStatsDTO getOwnerRatingStats(String ownerId) {
        List<Review> reviews = reviewRepository.findByStationOwner(ownerId);
        return calculateRatingStats(reviews);
    }

    private RatingStatsDTO calculateRatingStats(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new RatingStatsDTO(0.0, 0, new int[]{0, 0, 0, 0, 0});
        }

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Round to 2 decimal places
        average = Math.round(average * 100.0) / 100.0;

        int[] starCounts = new int[5]; // indices 0-4 for ratings 1-5
        reviews.forEach(review -> starCounts[review.getRating() - 1]++);

        return new RatingStatsDTO(average, reviews.size(), starCounts);
    }
}
