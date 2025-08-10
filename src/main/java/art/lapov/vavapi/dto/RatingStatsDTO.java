package art.lapov.vavapi.dto;

import lombok.Value;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

@Value
public class RatingStatsDTO {

    /**
     * Average rating (0.00 to 5.00)
     */
    @JsonProperty("average_rating")
    private Double averageRating;

    /**
     * Total number of reviews
     */
    @JsonProperty("total_reviews")
    private Integer totalReviews;

    /**
     * Array of number of star ratings [1★, 2★, 3★, 4★, 5★]
     * Index 0 = number of 1 star ratings
     * Index 1 = number of 2 star ratings
     * And so on...
     */
    @JsonProperty("star_counts")
    private int[] starCounts;

    // ===========================================
    // METHODS FOR EASY ACCESS TO DATA
    // ===========================================

    /**
     * Get the number of 1-star reviews
     */
    @JsonProperty("one_star_count")
    public int getOneStarCount() {
        return starCounts != null && starCounts.length > 0 ? starCounts[0] : 0;
    }

    /**
     * Get the number of reviews with 2 stars
     */
    @JsonProperty("two_star_count")
    public int getTwoStarCount() {
        return starCounts != null && starCounts.length > 1 ? starCounts[1] : 0;
    }

    /**
     * Get the number of reviews with 3 stars
     */
    @JsonProperty("three_star_count")
    public int getThreeStarCount() {
        return starCounts != null && starCounts.length > 2 ? starCounts[2] : 0;
    }

    /**
     * Get the number of reviews with 4 stars
     */
    @JsonProperty("four_star_count")
    public int getFourStarCount() {
        return starCounts != null && starCounts.length > 3 ? starCounts[3] : 0;
    }

    /**
     * Get the number of reviews with 5 stars
     */
    @JsonProperty("five_star_count")
    public int getFiveStarCount() {
        return starCounts != null && starCounts.length > 4 ? starCounts[4] : 0;
    }


    /**
     * Check to see if there are any reviews
     */
    @JsonProperty("has_reviews")
    public boolean hasReviews() {
        return totalReviews != null && totalReviews > 0;
    }

    /**
     * Create empty statistics (if no reviews)
     */
    public static RatingStatsDTO empty() {
        return new RatingStatsDTO(
                0.0,
                0,
                new int[]{0, 0, 0, 0, 0}
        );
    }

    /**
     * Create statistics with basic data
     */
    public static RatingStatsDTO of(Double averageRating, Integer totalReviews, int[] starCounts) {
        return new RatingStatsDTO(averageRating, totalReviews, starCounts);
    }

}


