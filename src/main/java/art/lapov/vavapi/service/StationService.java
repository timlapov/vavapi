package art.lapov.vavapi.service;

import art.lapov.vavapi.dto.StationCreateDTO;
import art.lapov.vavapi.dto.StationDTO;
import art.lapov.vavapi.dto.StationUpdateDTO;
import art.lapov.vavapi.exception.ResourceNotFoundException;
import art.lapov.vavapi.mapper.StationMapper;
import art.lapov.vavapi.model.Location;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.LocationRepository;
import art.lapov.vavapi.repository.StationRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final LocationRepository locationRepository;
    private final StationMapper stationMapper;

    public Page<StationDTO> findAll(Pageable pageable) {
        return stationRepository.findAll(pageable)
                .map(stationMapper::map);
    }

    public StationDTO findById(String id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + id));
        return stationMapper.map(station);
    }

    public StationDTO create(StationCreateDTO dto, User user) {
        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + dto.getLocationId()));

        // Verify that user owns the location
        if (!location.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("You can only create stations for your own locations");
        }

        Station station = stationMapper.map(dto);
        station.setLocation(location);
        station.setEnabled(true);
        station.setDeleted(false);

        Station savedStation = stationRepository.save(station);
        return stationMapper.map(savedStation);
    }

    public StationDTO update(String id, StationUpdateDTO dto) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + id));
        stationMapper.update(dto, station);
        Station updatedStation = stationRepository.save(station);
        return stationMapper.map(updatedStation);
    }

    public void delete(String id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + id));
        //TODO: check if station is used in reservations
        // Soft delete
        station.setDeleted(true);
        station.setEnabled(false);
        stationRepository.save(station);
    }

    public List<StationDTO> findByLocationId(String locationId) {
        return stationRepository.findByLocationIdAndEnabledTrue(locationId)
                .stream()
                .map(stationMapper::map)
                .toList();
    }

    public boolean isOwner(String stationId, String userId) {
        return stationRepository.findById(stationId)
                .map(station -> station.getLocation().getOwner().getId().equals(userId))
                .orElse(false);
    }

    public void updatePhoto(String stationId, String fileName) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found with id: " + stationId));
        station.setPhotoUrl(fileName);
        stationRepository.save(station);
    }

    public List<StationDTO> findAvailableByLocationAndPeriod(String locationId, LocalDateTime startDate, LocalDateTime endDate) {
        return stationRepository.findAvailableStationsByLocationAndPeriod(locationId, startDate, endDate)
                .stream()
                .map(stationMapper::map)
                .toList();
    }

}
