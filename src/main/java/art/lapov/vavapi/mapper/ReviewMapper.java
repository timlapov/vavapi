package art.lapov.vavapi.mapper;

import art.lapov.vavapi.dto.ReviewCreateDTO;
import art.lapov.vavapi.dto.ReviewDTO;
import art.lapov.vavapi.dto.ReviewUpdateDTO;
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
public abstract class ReviewMapper {
        public abstract Review map(ReviewCreateDTO dto);

        @Mapping(source = "author.firstName", target = "authorFirstName")
        @Mapping(source = "author.lastName", target = "authorLastName")
        public abstract ReviewDTO map(Review model);

        public abstract void update(ReviewUpdateDTO dto, @MappingTarget Review model);
}

