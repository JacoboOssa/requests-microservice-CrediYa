package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.dto.response.PetitionResponseDTO;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PetitionDTOMapper {
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "loanType", source = "dto", qualifiedByName = "mapLoanType")
    Petition toPetition(CreatePetitionDTO dto);

    PetitionResponseDTO toPetitionResponseDTO(Petition petition);

    @Named("mapLoanType")
    default LoanType mapLoanType(CreatePetitionDTO dto) {
        return LoanType.builder()
                .name(dto.loanTypeName())
                .build();    }
}
