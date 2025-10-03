package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.CostCalculationDTO;
import art.lapov.vavapi.dto.StationCreateDTO;
import art.lapov.vavapi.dto.StationDTO;
import art.lapov.vavapi.dto.StationUpdateDTO;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.PricingIntervalService;
import art.lapov.vavapi.service.StationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;
    private final PricingIntervalService pricingIntervalService;

    @GetMapping
    public Page<StationDTO> showAll(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        if (size > 45) size = 45;
        if (page < 1) page = 1;
        Pageable pageable = PageRequest.of(page - 1, size);
        return stationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public StationDTO showOne(@PathVariable String id) {
        return stationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationDTO create(@RequestBody @Valid StationCreateDTO dto,
                             @AuthenticationPrincipal User user) {
        return stationService.create(dto, user);
    }

    @PutMapping("/{id}")
    public StationDTO update(@PathVariable String id,
                             @RequestBody @Valid StationUpdateDTO dto,
                             @AuthenticationPrincipal User user) {
        if (!stationService.isOwner(id, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own stations");
        }
        return stationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, @AuthenticationPrincipal User user) {
        if (!stationService.isOwner(id, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own stations");
        }
        stationService.delete(id);
    }

    @GetMapping("/location/{locationId}")
    public List<StationDTO> findByLocation(
            @PathVariable String locationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // If both dates are provided, find available stations for the period
        if (startDate != null && endDate != null) {
            return stationService.findAvailableByLocationAndPeriod(locationId, startDate, endDate);
        }

        // Otherwise, return all active stations in the location
        return stationService.findByLocationId(locationId);
    }

    @GetMapping("/{id}/calculate-price")
    public CostCalculationDTO calculatePrice(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return pricingIntervalService.calculateCost(id, startTime, endTime);
    }

}

