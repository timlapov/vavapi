package art.lapov.vavapi.mapper;

import art.lapov.vavapi.dto.PricingIntervalCreateDTO;
import art.lapov.vavapi.dto.PricingIntervalDTO;
import art.lapov.vavapi.dto.PricingIntervalUpdateDTO;
import art.lapov.vavapi.model.PricingInterval;
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
public abstract class PricingIntervalMapper {
    public abstract PricingInterval map(PricingIntervalCreateDTO dto);
    public abstract PricingIntervalDTO map(PricingInterval model);
    public abstract void update(PricingIntervalUpdateDTO dto, @MappingTarget PricingInterval model);
}
