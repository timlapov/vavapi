package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.RatingStatsDTO;
import art.lapov.vavapi.service.RatingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    /**
     * Get a station rating
     */
    @GetMapping("/station/{stationId}")
    public RatingStatsDTO getStationRating(@PathVariable String stationId) {
        RatingStatsDTO stats = ratingService.getStationRatingStats(stationId);

        // If there are no reviews, return an empty statistic
        return stats != null ? stats : RatingStatsDTO.empty();
    }

    /**
     * Get the owner's rating (for all his stations)
     */
    @GetMapping("/owner/{ownerId}")
    public RatingStatsDTO getOwnerRating(@PathVariable String ownerId) {
        return ratingService.getOwnerRatingStats(ownerId);
    }
}

