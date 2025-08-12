package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.CostCalculationDTO;
import art.lapov.vavapi.dto.PricingIntervalCreateDTO;
import art.lapov.vavapi.dto.PricingIntervalDTO;
import art.lapov.vavapi.dto.PricingIntervalUpdateDTO;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.PricingIntervalService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/pricing-intervals")
public class PricingIntervalController {

    private final PricingIntervalService pricingIntervalService;

    /**
     * Get all pricing intervals for a station
     * Public endpoint - anyone can see pricing
     */
    @GetMapping("/station/{stationId}")
    public List<PricingIntervalDTO> getStationPricingIntervals(@PathVariable String stationId) {
        return pricingIntervalService.findByStationId(stationId);
    }

    /**
     * Create a new pricing interval for a station
     * Only station owner can create
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricingIntervalDTO create(@RequestBody @Valid PricingIntervalCreateDTO dto,
                                     @AuthenticationPrincipal User user) {
        return pricingIntervalService.create(dto, user);
    }

    /**
     * Update an existing pricing interval
     * Only station owner can update
     */
    @PutMapping("/{id}")
    public PricingIntervalDTO update(@PathVariable String id,
                                     @RequestBody @Valid PricingIntervalUpdateDTO dto,
                                     @AuthenticationPrincipal User user) {
        return pricingIntervalService.update(id, dto, user);
    }

    /**
     * Delete a pricing interval
     * Only station owner can delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       @AuthenticationPrincipal User user) {
        pricingIntervalService.delete(id, user);
    }

    /**
     * Calculate cost for a potential reservation
     * Public endpoint - anyone can calculate costs
     */
    @GetMapping("/calculate-cost")
    public CostCalculationDTO calculateCost(
            @RequestParam String stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return pricingIntervalService.calculateCost(stationId, startTime, endTime);
    }

    /**
     * Check if station is available during specified period
     * Public endpoint
     */
    @GetMapping("/check-availability")
    public Map<String, Boolean> checkAvailability(
            @RequestParam String stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        boolean available = pricingIntervalService.isStationAvailable(stationId, startTime, endTime);
        return Map.of("available", available);
    }
}