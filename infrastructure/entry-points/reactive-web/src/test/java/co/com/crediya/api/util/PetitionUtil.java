package co.com.crediya.api.util;

import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.dto.request.UpdateStatusPetitionDTO;
import co.com.crediya.api.dto.response.PetitionResponseDTO;
import co.com.crediya.model.dto.ListPetitionsDTO;
import co.com.crediya.model.petition.Petition;

import java.math.BigDecimal;

public class PetitionUtil {
    public static Petition petition() {
        return Petition.builder()
                .id("1")
                .term(12)
                .amount(BigDecimal.valueOf(2460000))
                .email("jq@gmail.com")
                .status(StatusUtil.status())
                .loanType(LoanTypeUtil.loanType())
                .build();
    }

    public static CreatePetitionDTO createPetitionDTO(){
        return CreatePetitionDTO.builder()
                .identificationNumber("1234567890")
                .amount(BigDecimal.valueOf(200000))
                .term(23)
                .loanTypeName("loan")
                .build();
    }

    public static PetitionResponseDTO petitionResponseDTO(){
        return PetitionResponseDTO.builder()
                .term(12)
                .amount(BigDecimal.valueOf(2000000))
                .email("jaco@gmail.com")
                .status(StatusUtil.statusResponseDTO())
                .loanType(LoanTypeUtil.loanTypeResponseDTO())
                .build();
    }

    public static ListPetitionsDTO listPetitionsDTO(){
        return ListPetitionsDTO.builder()
                .id("1")
                .email("mq@gmail.com")
                .name("Angela")
                .lastName("Guarnizo")
                .baseSalary(BigDecimal.valueOf(3450000))
                .term(12)
                .amount(BigDecimal.valueOf(5000000))
                .loanTypeName("Personal")
                .statusName("MANUAL_REVISION")
                .totalMonthlyDebtApproved(BigDecimal.ZERO)
                .build();
    }

    public static UpdateStatusPetitionDTO updateStatusPetitionDTO() {
        return UpdateStatusPetitionDTO.builder()
                .status("APPROVED") // o el valor que uses en tus tests
                .build();
    }
}
