package art.lapov.vavapi.mapper;

import art.lapov.vavapi.dto.ReservationCreateDTO;
import art.lapov.vavapi.dto.ReservationDTO;
import art.lapov.vavapi.dto.ReviewCreateDTO;
import art.lapov.vavapi.dto.ReviewDTO;
import art.lapov.vavapi.dto.ReviewUpdateDTO;
import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.Review;
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
public abstract class ReservationMapper {
    public abstract Reservation map(ReservationCreateDTO dto);

    @Mapping(source = "review.id", target = "reviewId")
    @Mapping(source = "review.rating", target = "reviewRating")
    public abstract ReservationDTO map(Reservation model);
    //public abstract void update(ReservationUpdateDTO dto, @MappingTarget Reservation model);
}
