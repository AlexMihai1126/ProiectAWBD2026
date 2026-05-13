package ro.fmi.awbd.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ro.fmi.awbd.model.entity.dto.mapper.LocationMapper;
import ro.fmi.awbd.model.entity.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.entity.dto.request.LocationUpdateRequest;
import ro.fmi.awbd.model.entity.dto.response.LocationResponse;
import ro.fmi.awbd.model.entity.LocationEntity;
import ro.fmi.awbd.repository.LocationRepository;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Transactional
    public LocationResponse createLocation(LocationCreateRequest request) {
        LocationEntity location = locationMapper.toEntity(request);
        LocationEntity saved = locationRepository.save(location);
        return locationMapper.toResponse(saved);
    }

    @Transactional
    public LocationResponse updateLocation(Long locationId, LocationUpdateRequest request) {
        LocationEntity location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));

        locationMapper.updateEntity(request, location);
        LocationEntity saved = locationRepository.save(location);
        return locationMapper.toResponse(saved);
    }
}
