package co.com.crediya.r2dbc.helper;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.r2dbc.entity.PetitionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PetitionMapper {
    @Mapping(target = "statusId", source = "status.id")
    @Mapping(target = "loanTypeId", source = "loanType.id")
    PetitionEntity toEntity(Petition petition);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "loanType", ignore = true)
    Petition toDomain(PetitionEntity entity);
}
