package art.lapov.vavapi.mapper;

import art.lapov.vavapi.dto.LocationCreateDTO;
import art.lapov.vavapi.dto.LocationDTO;
import art.lapov.vavapi.dto.LocationUpdateDTO;
import art.lapov.vavapi.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = ReferenceMapper.class,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class LocationMapper {
    @Mapping(target = "owner", source = "ownerId")
    public abstract Location map(LocationCreateDTO dto);
    public abstract LocationDTO map(Location model);
    public abstract void update(LocationUpdateDTO dto, @MappingTarget Location model);
}
