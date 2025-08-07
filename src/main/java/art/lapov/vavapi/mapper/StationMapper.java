package art.lapov.vavapi.mapper;
import art.lapov.vavapi.dto.StationCreateDTO;
import art.lapov.vavapi.dto.StationDTO;
import art.lapov.vavapi.dto.StationUpdateDTO;
import art.lapov.vavapi.model.Station;
import org.mapstruct.Mapper;
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
public abstract class StationMapper {
    public abstract Station map(StationCreateDTO dto);
    public abstract StationDTO map(Station model);
    public abstract void update(StationUpdateDTO dto, @MappingTarget Station model);
}
