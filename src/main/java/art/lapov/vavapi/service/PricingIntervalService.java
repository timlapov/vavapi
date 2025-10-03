package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.CostCalculationDTO;
import art.lapov.vavapi.dto.PricingIntervalCreateDTO;
import art.lapov.vavapi.dto.PricingIntervalDTO;
import art.lapov.vavapi.dto.PricingIntervalUpdateDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.mapper.PricingIntervalMapper;
import art.lapov.vavapi.model.PricingInterval;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.PricingIntervalRepository;
import art.lapov.vavapi.repository.StationRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class PricingIntervalService {

    private final PricingIntervalRepository pricingIntervalRepository;
    private final StationRepository stationRepository;
    private final PricingIntervalMapper pricingIntervalMapper;

    /**
     * Get all pricing intervals for a station
     */
    public List<PricingIntervalDTO> findByStationId(String stationId) {
        // Verify station exists
        if (!stationRepository.existsById(stationId)) {
            throw new ResourceNotFoundException("Station not found with id: " + stationId);
        }

        return pricingIntervalRepository.findByStationIdOrderByStartHour(stationId)
                .stream()
                .map(pricingIntervalMapper::map)
                .toList();
    }

    /**
     * Create a new pricing interval
     */
    @Transactional
    public PricingIntervalDTO create(PricingIntervalCreateDTO dto, User user) {
        // Verify station exists and user is owner
        Station station = stationRepository.findById(dto.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + dto.getStationId()));

        if (!station.getLocation().getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only create pricing intervals for your own stations");
        }

        // Validate time interval
        validateTimeInterval(dto.getStartHour(), dto.getEndHour());

        // Check for overlapping intervals
        checkForOverlappingIntervals(dto.getStationId(), dto.getStartHour(), dto.getEndHour(), null);

        PricingInterval pricingInterval = pricingIntervalMapper.map(dto);
        pricingInterval.setStation(station);

        PricingInterval saved = pricingIntervalRepository.save(pricingInterval);
        return pricingIntervalMapper.map(saved);
    }

    /**
     * Update an existing pricing interval
     */
    @Transactional
    public PricingIntervalDTO update(String id, PricingIntervalUpdateDTO dto, User user) {
        PricingInterval pricingInterval = pricingIntervalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing interval not found with id: " + id));

        // Verify user is owner
        if (!pricingInterval.getStation().getLocation().getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only update pricing intervals for your own stations");
        }

        // Validate time interval
        validateTimeInterval(dto.getStartHour(), dto.getEndHour());

        // Check for overlapping intervals (excluding current one)
        checkForOverlappingIntervals(
                pricingInterval.getStation().getId(),
                dto.getStartHour(),
                dto.getEndHour(),
                id
        );

        pricingIntervalMapper.update(dto, pricingInterval);
        PricingInterval updated = pricingIntervalRepository.save(pricingInterval);
        return pricingIntervalMapper.map(updated);
    }

    /**
     * Delete a pricing interval
     */
    @Transactional
    public void delete(String id, User user) {
        PricingInterval pricingInterval = pricingIntervalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing interval not found with id: " + id));

        // Verify user is owner
        if (!pricingInterval.getStation().getLocation().getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only delete pricing intervals for your own stations");
        }

        pricingIntervalRepository.deleteById(id);
    }

    /**
     * Calculate total cost for a reservation
     */
    public CostCalculationDTO calculateCost(String stationId, LocalDateTime startTime, LocalDateTime endTime) {
        // Verify station exists
        stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + stationId));

        // Validate reservation times
        if (startTime.isAfter(endTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot make reservations in the past");
        }

        // Get pricing intervals for the station
        List<PricingInterval> intervals = pricingIntervalRepository.findByStationIdOrderByStartHour(stationId);

        if (intervals.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No pricing intervals defined for this station");
        }

        if (!isTimeCoveredByIntervals(startTime.toLocalTime(), endTime.toLocalTime(), intervals)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selected time period is not fully covered by pricing intervals");
        }

        // Calculate total cost
        int totalCostInCents = calculateTotalCost(intervals, startTime, endTime);

        // Calculate duration in hours
        double durationHours = Duration.between(startTime, endTime).toMinutes() / 60.0;

        return new CostCalculationDTO(
                stationId,
                startTime,
                endTime,
                totalCostInCents,
                durationHours
        );
    }

    /**
     * Check if station is available during the specified time period
     */
    public boolean isStationAvailable(String stationId, LocalDateTime startTime, LocalDateTime endTime) {
        List<PricingInterval> intervals = pricingIntervalRepository.findByStationIdOrderByStartHour(stationId);

        if (intervals.isEmpty()) {
            return false; // No pricing intervals = station not available
        }

        // For MVP, we check if the time period is covered by pricing intervals
        // This is a simplified check
        LocalTime startTimeOfDay = startTime.toLocalTime();
        LocalTime endTimeOfDay = endTime.toLocalTime();

        // Check if the entire period is covered by pricing intervals
        return isTimeCoveredByIntervals(startTimeOfDay, endTimeOfDay, intervals);
    }

    // ================ PRIVATE HELPER METHODS ================

    private void validateTimeInterval(LocalTime startHour, LocalTime endHour) {
        if (startHour == null || endHour == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start hour and end hour are required");
        }

        if (startHour.equals(endHour) && !startHour.equals(LocalTime.MIDNIGHT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start hour and end hour cannot be the same (except for 00:00-00:00 full-day intervals)");
        }


        // For simplicity in MVP, we don't allow intervals that cross midnight
        if (startHour.isAfter(endHour)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Intervals crossing midnight are not supported in current version");
        }
    }

    private void checkForOverlappingIntervals(String stationId, LocalTime startHour,
                                              LocalTime endHour, String excludeId) {
        List<PricingInterval> existingIntervals = pricingIntervalRepository.findByStationId(stationId);

        for (PricingInterval interval : existingIntervals) {
            // Skip the interval being updated
            if (interval.getId().equals(excludeId)) {
                continue;
            }

            // Check for overlap
            if (isOverlapping(startHour, endHour, interval.getStartHour(), interval.getEndHour())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("Time interval %s-%s overlaps with existing interval %s-%s",
                                startHour, endHour, interval.getStartHour(), interval.getEndHour()));
            }
        }
    }

    private boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Two intervals overlap if start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private int calculateTotalCost(List<PricingInterval> intervals,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        int totalCostInCents = 0;
        LocalDateTime currentTime = startTime;

        // For MVP, we calculate cost day by day
        while (currentTime.isBefore(endTime)) {
            LocalDateTime dayEnd = currentTime.toLocalDate().plusDays(1).atStartOfDay();
            if (dayEnd.isAfter(endTime)) {
                dayEnd = endTime;
            }

            // Calculate cost for this day
            totalCostInCents += calculateDailyCost(intervals, currentTime, dayEnd);

            // Move to next day
            currentTime = currentTime.toLocalDate().plusDays(1).atStartOfDay();
        }

        return totalCostInCents;
    }

    private int calculateDailyCost(List<PricingInterval> intervals,
                                   LocalDateTime dayStartInclusive,
                                   LocalDateTime dayEndExclusive) {
        int costInCents = 0;
        for (PricingInterval interval : intervals) {
            // Build interval bounds for this date
            LocalDate base = dayStartInclusive.toLocalDate();
            LocalDateTime intervalStart = base.atTime(interval.getStartHour());
            LocalDateTime intervalEndExclusive = toExclusiveEnd(base, interval.getEndHour());

            // Overlap on [start, end)
            LocalDateTime overlapStart = intervalStart.isAfter(dayStartInclusive) ? intervalStart : dayStartInclusive;
            LocalDateTime overlapEnd = intervalEndExclusive.isBefore(dayEndExclusive) ? intervalEndExclusive : dayEndExclusive;

            if (overlapStart.isBefore(overlapEnd)) {
                long minutes = java.time.Duration.between(overlapStart, overlapEnd).toMinutes(); // exact minutes
                // bill by minutes to avoid per-interval rounding drift
                costInCents += (int) Math.round((minutes / 60.0) * interval.getHourlyPriceInCents());
            }
        }
        return costInCents;
    }

    private boolean isTimeCoveredByIntervals(LocalTime startTime, LocalTime endTime,
                                             List<PricingInterval> intervals) {
        // Sort intervals by start time
        List<PricingInterval> sortedIntervals = new ArrayList<>(intervals);
        sortedIntervals.sort(Comparator.comparing(PricingInterval::getStartHour));

        // Check if the entire period is covered
        LocalTime currentTime = startTime;

        for (PricingInterval interval : sortedIntervals) {
            if (interval.getStartHour().isAfter(currentTime)) {
                // Gap found
                return false;
            }

            if (interval.getEndHour().isAfter(currentTime)) {
                currentTime = interval.getEndHour();
            }

            if (!currentTime.isBefore(endTime)) {
                // Entire period is covered
                return true;
            }
        }

        return currentTime.isAfter(endTime) || currentTime.equals(endTime);
    }

    /**
     * Check if user is owner of the station (helper method)
     */
    public boolean isOwner(String stationId, String userId) {
        return stationRepository.findById(stationId)
                .map(station -> station.getLocation().getOwner().getId().equals(userId))
                .orElse(false);
    }

    private static LocalDateTime toExclusiveEnd(java.time.LocalDate date, java.time.LocalTime end) {
        // Treat 00:00 as midnight (exclusive -> next day start)
        if (end.equals(java.time.LocalTime.MIDNIGHT)) {
            return date.plusDays(1).atStartOfDay();
        }
        // Many UIs store "full day" as 23:59. Interpret it as midnight exclusive.
        if (end.equals(java.time.LocalTime.of(23, 59))) {
            return date.plusDays(1).atStartOfDay();
        }
        return date.atTime(end); // default: same day, end-exclusive
    }
}