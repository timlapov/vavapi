package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.ReviewCreateDTO;
import art.lapov.vavapi.dto.ReviewDTO;
import art.lapov.vavapi.dto.ReviewUpdateDTO;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Create a review of the reservation
     */
    @PostMapping("/reservation/{reservationId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDTO createReservationReview(
            @PathVariable String reservationId,
            @RequestBody @Valid ReviewCreateDTO dto,
            @AuthenticationPrincipal User user) {
        return reviewService.createReservationReview(reservationId, dto, user);
    }

    /**
     * Get feedback on the reservation
     */
    @GetMapping("/reservation/{reservationId}")
    public ReviewDTO getReservationReview(@PathVariable String reservationId) {
        ReviewDTO review = reviewService.findByReservationId(reservationId);
        if (review == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found");
        }
        return review;
    }

    /**
     * Get feedback on the station
     */
    @GetMapping("/station/{stationId}")
    public Page<ReviewDTO> getStationReviews(
            @PathVariable String stationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return reviewService.findByStationId(stationId, pageable);
    }

    /**
     * Get feedback on the owner
     */
    @GetMapping("/owner/{ownerId}")
    public Page<ReviewDTO> getOwnerReviews(@PathVariable String ownerId, @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "5") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return reviewService.findByOwnerId(ownerId, pageable);
    }

    /**
     * Get my reviews
     */
    @GetMapping("/my")
    public List<ReviewDTO> getMyReviews(@AuthenticationPrincipal User user) {
        return reviewService.findByAuthor(user);
    }

    /**
     * Update Review
     */
    @PutMapping("/{id}")
    public ReviewDTO updateReview(
            @PathVariable String id,
            @RequestBody @Valid ReviewUpdateDTO dto,
            @AuthenticationPrincipal User user) {
        return reviewService.update(id, dto, user);
    }

    /**
     * Delete review
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable String id, @AuthenticationPrincipal User user) {
        reviewService.delete(id, user);
    }
}
