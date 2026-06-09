package ro.fmi.awbd.model.dto.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ro.fmi.awbd.model.dto.request.LocationCreateRequest;
import ro.fmi.awbd.model.dto.request.LocationUpdateRequest;
import ro.fmi.awbd.model.dto.response.LocationResponse;
import ro.fmi.awbd.model.entity.LocationEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    LocationEntity toEntity(LocationCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(LocationUpdateRequest request, @MappingTarget LocationEntity entity);

    LocationResponse toResponse(LocationEntity entity);
}
