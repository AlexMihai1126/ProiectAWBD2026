package ro.fmi.awbd.model.dto.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ro.fmi.awbd.model.dto.request.MediaCreateRequest;
import ro.fmi.awbd.model.dto.request.MediaUpdateRequest;
import ro.fmi.awbd.model.dto.response.MediaResponse;
import ro.fmi.awbd.model.entity.MediaEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MediaMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoot", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MediaEntity toEntity(MediaCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shoot", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(MediaUpdateRequest request, @MappingTarget MediaEntity entity);

    @Mapping(target = "shootId", source = "shoot.id")
    MediaResponse toResponse(MediaEntity entity);
}
