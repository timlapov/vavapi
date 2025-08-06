package art.lapov.vavapi.service;

import art.lapov.vavapi.repository.LocationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
class LocationService {
    private LocationRepository locationRepository;


}
