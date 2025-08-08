package art.lapov.vavapi.service;

import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.repository.StationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class StationService {

    private final StationRepository stationRepository;
//    // Adds a new station
//    StationDto addStation(StationDto data);
//
//    // Updates an existing station
//    StationDto updateStation(String stationId, StationDto data);
//
//    // Returns all stations
//    List<StationDto> findAllStations();
//
//    // Returns a station by its ID
//    StationDto findStationById(String stationId);
//
//    // Finds available stations by given search criteria
//    List<StationDto> findAvailableStations(SearchCriteria criteria);
//
//    // Gets detailed information about a station
//    StationDto getStationDetails(String stationId);

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

}
