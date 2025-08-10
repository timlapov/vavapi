package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.LocationCreateDTO;
import art.lapov.vavapi.dto.LocationDTO;
import art.lapov.vavapi.dto.LocationUpdateDTO;
import art.lapov.vavapi.dto.StationDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.mapper.LocationMapper;
import art.lapov.vavapi.mapper.StationMapper;
import art.lapov.vavapi.model.Location;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.LocationRepository;
import art.lapov.vavapi.repository.StationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@AllArgsConstructor
public class LocationService {
    private LocationRepository locationRepository;
    private LocationMapper locationMapper;
    private StationRepository stationRepository;
    private StationMapper stationMapper;

    public Page<LocationDTO> findAll(Pageable pageable) {
        return locationRepository.findAll(pageable).map(item -> locationMapper.map(item));
    }

    public LocationDTO findById(String id) {
        Location location = locationRepository.findById(id).orElseThrow();
        return locationMapper.map(location);
    }

    public LocationDTO create(LocationCreateDTO dto, User user) {

        Location location = locationMapper.map(dto);
        location.setOwner(user);
        location.setDeleted(false);
        Location savedLocation = locationRepository.save(location);
        return locationMapper.map(savedLocation);
    }

    public LocationDTO update(String id, LocationUpdateDTO dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        locationMapper.update(dto, location);
        Location updatedLocation = locationRepository.save(location);
        return locationMapper.map(updatedLocation);
    }

    public void delete(String id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        // TODO: check if location has stations
        location.setDeleted(true);
        locationRepository.save(location);
    }

    public List<LocationDTO> findWithinRadius(double latitude, double longitude, double radiusKm) {
        if (radiusKm > 30 || radiusKm < 0.01) {
            radiusKm = 30;
        }
        List<Location> locations = locationRepository.findWithinRadius(latitude, longitude, radiusKm);
        return locations.stream()
                .map(locationMapper::map)
                .toList();
    }

    public List<StationDTO> findStationsByLocation(String locationId) {
        List<Station> stations = stationRepository.findByLocationIdAndEnabledTrue(locationId);
        return stations.stream()
                .map(stationMapper::map)
                .toList();
    }

    public void updatePhoto(String locationId, String fileName) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        location.setPhotoUrl(fileName);
        locationRepository.save(location);
    }

    public boolean isOwner(String locationId, String userId) {
        return locationRepository.findById(locationId)
                .map(location -> location.getOwner().getId().equals(userId))
                .orElse(false);
    }

}
