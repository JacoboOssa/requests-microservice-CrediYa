package co.com.crediya.r2dbc.helper;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
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

    @Mapping(target = "status", expression = "java(mapStatus(entity.getStatusId()))")
    @Mapping(target = "loanType", expression = "java(mapLoanType(entity.getLoanTypeId()))")
    Petition toModel(PetitionEntity entity);

    default Status mapStatus(String statusId) {
        if (statusId == null) return null;
        return Status.builder().id(statusId).build();
    }

    default LoanType mapLoanType(String loanTypeId) {
        if (loanTypeId == null) return null;
        return LoanType.builder().id(loanTypeId).build();
    }


}
