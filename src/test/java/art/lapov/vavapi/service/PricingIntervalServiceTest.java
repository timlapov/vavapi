package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.CostCalculationDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.model.PricingInterval;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.repository.PricingIntervalRepository;
import art.lapov.vavapi.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingIntervalServiceTest {

    @Mock
    private PricingIntervalRepository pricingIntervalRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private PricingIntervalService pricingIntervalService;

    private String stationId = "station-123";
    private Station station;

    @BeforeEach
    void setUp() {
        station = new Station();
        station.setId(stationId);
    }

    @Test
    void calculateCost_SingleInterval_SameDay() {
        // Given: rate 20€/hour from 08:00 to 20:00
        PricingInterval interval = createPricingInterval(
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                2000 // 20€
        );

        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 14, 0); // 4 часа

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(List.of(interval));

        // When
        CostCalculationDTO result = pricingIntervalService.calculateCost(stationId, start, end);

        // Then
        assertEquals(8000, result.getTotalCostInCents()); // 4 hours * 20€ = 80€
        assertEquals(4.0, result.getDurationHours());
    }

    @Test
    void calculateCost_MultipleIntervals_SameDay() {
        // Given: morning time 15€/hour, evening time 25€/hour
        PricingInterval morning = createPricingInterval(
                LocalTime.of(6, 0),
                LocalTime.of(12, 0),
                1500
        );
        PricingInterval evening = createPricingInterval(
                LocalTime.of(12, 0),
                LocalTime.of(22, 0),
                2500
        );

        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 16, 0); // 6 часов

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(Arrays.asList(morning, evening));

        // When
        CostCalculationDTO result = pricingIntervalService.calculateCost(stationId, start, end);

        // Then
        // 2 hours in the morning (10:00-12:00) * 15€ = 30€
        // 4 hours in the evening (12:00-16:00) * 25€ = 100€
        assertEquals(13000, result.getTotalCostInCents()); // 130€
    }

    @Test
    void calculateCost_AcrossMidnight() {
        // Given: 24-hour rate 20€/hour
        PricingInterval allDay = createPricingInterval(
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                2000
        );

        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 22, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 16, 2, 0); // 4 hours after midnight

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(List.of(allDay));

        // When
        CostCalculationDTO result = pricingIntervalService.calculateCost(stationId, start, end);

        // Then
        assertEquals(8000, result.getTotalCostInCents()); // 4 hours * 20€
        assertEquals(4.0, result.getDurationHours());
    }

    @Test
    void calculateCost_MultipleDays() {
        // Given: rate 10€/hour from 08:00 to 20:00
        PricingInterval dayInterval = createPricingInterval(
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                1000
        );

        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 17, 18, 0); // 2 days and 8 hours

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(List.of(dayInterval));

        // When
        CostCalculationDTO result = pricingIntervalService.calculateCost(stationId, start, end);

        // Then
        // Day 1: 10:00-20:00 = 10 hours * 10€ = 100€
        // Day 2: 08:00-20:00 = 12 hours * 10€ = 120€
        // Day 3: 08:00-18:00 = 10 hours * 10€ = 100€
        assertEquals(32000, result.getTotalCostInCents()); // 320€
    }

    @Test
    void calculateCost_PartialIntervalCoverage() {
        // Given: tariff only from 10:00 to 18:00
        PricingInterval limitedInterval = createPricingInterval(
                LocalTime.of(10, 0),
                LocalTime.of(18, 0),
                3000
        );

        // Reservation from 08:00 to 20:00.
        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 20, 0);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(List.of(limitedInterval));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                pricingIntervalService.calculateCost(stationId, start, end)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Selected time period is not fully covered by pricing intervals"));
    }

    @Test
    void calculateCost_NoIntervals_ThrowsException() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 12, 0);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(ResponseStatusException.class, () ->
                pricingIntervalService.calculateCost(stationId, start, end)
        );
    }

    @Test
    void calculateCost_InvalidDates_ThrowsException() {
        // Given: end date earlier than start date
        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 14, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 10, 0);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));

        // When & Then
        assertThrows(ResponseStatusException.class, () ->
                pricingIntervalService.calculateCost(stationId, start, end)
        );
    }

    @Test
    void calculateCost_FractionalHours() {
        // Given: reservation for 2.5 hours.
        PricingInterval interval = createPricingInterval(
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                1000 // 10€/h
        );

        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 12, 30);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pricingIntervalRepository.findByStationIdOrderByStartHour(stationId))
                .thenReturn(List.of(interval));

        // When
        CostCalculationDTO result = pricingIntervalService.calculateCost(stationId, start, end);

        // Then
        assertEquals(2500, result.getTotalCostInCents()); // 2.5 * 10€
        assertEquals(2.5, result.getDurationHours());
    }

    @Test
    void calculateCost_StationNotFound_ThrowsException() {
        // Given
        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 12, 0);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                pricingIntervalService.calculateCost(stationId, start, end)
        );
    }

    // Helper method
    private PricingInterval createPricingInterval(LocalTime start, LocalTime end, Integer priceInCents) {
        PricingInterval interval = new PricingInterval();
        interval.setStartHour(start);
        interval.setEndHour(end);
        interval.setHourlyPriceInCents(priceInCents);
        return interval;
    }

}