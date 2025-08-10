package art.lapov.vavapi.controller;

import art.lapov.vavapi.dto.LocationCreateDTO;
import art.lapov.vavapi.dto.LocationDTO;
import art.lapov.vavapi.dto.LocationUpdateDTO;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.LocationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locationService;

    @GetMapping("")
    public Page<LocationDTO> showAll(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "20") Integer size) {
        if (size > 45) {
            size = 45;
        }
        if (page < 1) {
            page = 1;
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        return locationService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public LocationDTO showOne(@PathVariable String id) {
        return locationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDTO create(@RequestBody @Valid LocationCreateDTO dto,
                              @AuthenticationPrincipal User user) {
        return locationService.create(dto, user);
    }

    @PutMapping("/{id}")
    public LocationDTO update(@PathVariable String id,
                              @RequestBody @Valid LocationUpdateDTO dto,
                              @AuthenticationPrincipal User user) {
       if (!locationService.isOwner(id, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own locations");
        }
        return locationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       @AuthenticationPrincipal User user) {
        if (!locationService.isOwner(id, user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own locations");
        }
        locationService.delete(id);
    }

    @GetMapping("/search")
    public List<LocationDTO> findNearby(@RequestParam double latitude,
                                        @RequestParam double longitude,
                                        @RequestParam(defaultValue = "10.0") double radius) {
        return locationService.findWithinRadius(latitude, longitude, radius);
    }

}
