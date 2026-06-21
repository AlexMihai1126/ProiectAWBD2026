package ro.fmi.awbd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ro.fmi.awbd.exception.ResourceNotFoundException;
import ro.fmi.awbd.model.dto.mapper.LocationMapper;
import ro.fmi.awbd.model.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.dto.request.LocationUpdateRequest;
import ro.fmi.awbd.model.dto.response.LocationResponse;
import ro.fmi.awbd.model.entity.LocationEntity;
import ro.fmi.awbd.repository.LocationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    @Test
    void getLocationReturnsMappedEntity() {
        LocationEntity entity = LocationEntity.builder().id(1L).name("Studio").build();
        LocationResponse response = LocationResponse.builder().id(1L).name("Studio").build();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(locationMapper.toResponse(entity)).thenReturn(response);

        assertThat(locationService.getLocation(1L)).isEqualTo(response);
    }

    @Test
    void createLocationPersistsEntity() {
        LocationCreateRequest request = LocationCreateRequest.builder().name("Park").build();
        LocationEntity mapped = LocationEntity.builder().name("Park").build();
        LocationEntity saved = LocationEntity.builder().id(2L).name("Park").build();
        LocationResponse response = LocationResponse.builder().id(2L).name("Park").build();
        when(locationMapper.toEntity(request)).thenReturn(mapped);
        when(locationRepository.save(mapped)).thenReturn(saved);
        when(locationMapper.toResponse(saved)).thenReturn(response);

        assertThat(locationService.createLocation(request)).isEqualTo(response);
    }

    @Test
    void getLocationsReturnsPage() {
        LocationEntity entity = LocationEntity.builder().id(3L).name("Beach").build();
        LocationResponse response = LocationResponse.builder().id(3L).name("Beach").build();
        PageRequest pageable = PageRequest.of(0, 5);
        when(locationRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(locationMapper.toResponse(entity)).thenReturn(response);

        assertThat(locationService.getLocations(pageable).getTotalElements()).isEqualTo(1);
    }

    @Test
    void deleteLocationRemovesEntity() {
        LocationEntity entity = LocationEntity.builder().id(4L).name("Old").build();
        when(locationRepository.findById(4L)).thenReturn(Optional.of(entity));

        locationService.deleteLocation(4L);

        verify(locationRepository).delete(entity);
    }

    @Test
    void getAllLocationsReturnsMappedList() {
        LocationEntity entity = LocationEntity.builder().id(5L).name("Hall").build();
        LocationResponse response = LocationResponse.builder().id(5L).name("Hall").build();
        when(locationRepository.findAll()).thenReturn(List.of(entity));
        when(locationMapper.toResponse(entity)).thenReturn(response);

        assertThat(locationService.getAllLocations()).containsExactly(response);
    }

    @Test
    void updateLocationUpdatesExistingEntity() {
        LocationEntity entity = LocationEntity.builder().id(6L).name("Old").build();
        LocationUpdateRequest request = LocationUpdateRequest.builder().name("New").build();
        when(locationRepository.findById(6L)).thenReturn(Optional.of(entity));
        when(locationRepository.save(entity)).thenReturn(entity);
        when(locationMapper.toResponse(entity)).thenReturn(LocationResponse.builder().id(6L).name("New").build());

        locationService.updateLocation(6L, request);

        verify(locationMapper).updateEntity(request, entity);
    }

    @Test
    void getLocationThrowsWhenMissing() {
        when(locationRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.getLocation(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
