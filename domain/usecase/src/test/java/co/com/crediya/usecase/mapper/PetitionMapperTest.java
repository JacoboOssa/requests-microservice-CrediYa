package co.com.crediya.usecase.mapper;

import co.com.crediya.model.dto.ApprovedLoanDTO;
import co.com.crediya.model.dto.ListPetitionsDTO;
import co.com.crediya.model.dto.ValidationSqsMessage;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.dto.PetitionSqsMessage;
import co.com.crediya.usecase.dto.ReportSqsMessage;
import co.com.crediya.usecase.util.LoanTypeUtil;
import co.com.crediya.usecase.util.PetitionUtil;
import co.com.crediya.usecase.util.StatusUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PetitionMapperTest {

    private final PetitionMapper mapper = new PetitionMapper();

    @Test
    void mustMapToListPetitionsDTO() {
        Petition petition = PetitionUtil.petition();
        Status status = StatusUtil.status();
        LoanType loanType = LoanTypeUtil.loanType();
        User user = User.builder()
                .name("Carlos")
                .lastName("Ossa")
                .email(petition.getEmail())
                .baseSalary(BigDecimal.valueOf(3_000_000))
                .build();

        Petition approved = Petition.builder()
                .id("2")
                .amount(BigDecimal.valueOf(1_000_000))
                .term(10)
                .loanType(loanType)
                .status(status)
                .build();

        ListPetitionsDTO dto = mapper.mapToDTO(petition, status, loanType, user, List.of(approved));

        assertThat(dto.getId()).isEqualTo(petition.getId());
        assertThat(dto.getEmail()).isEqualTo(petition.getEmail());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getLastName()).isEqualTo(user.getLastName());
        assertThat(dto.getLoanTypeName()).isEqualTo(loanType.getName());
        assertThat(dto.getStatusName()).isEqualTo(status.getName());
        assertThat(dto.getTotalMonthlyDebtApproved()).isEqualTo(
                approved.getAmount().divide(BigDecimal.valueOf(approved.getTerm()))
        );
    }

    @Test
    void mustMapToPetitionSqsMessage() {
        Petition petition = PetitionUtil.petition();

        PetitionSqsMessage message = mapper.toSqsMessage(petition);

        assertThat(message.getPetitionId()).isEqualTo(petition.getId());
        assertThat(message.getEmail()).isEqualTo(petition.getEmail());
        assertThat(message.getStatusName()).isEqualTo(petition.getStatus().getName());
        assertThat(message.getLoanTypeName()).isEqualTo(petition.getLoanType().getName());
        assertThat(message.getAmount()).isEqualTo(petition.getAmount());
        assertThat(message.getTerm()).isEqualTo(petition.getTerm());
    }

    @Test
    void mustMapToValidationSqsMessage() {
        Petition petition = PetitionUtil.petition();
        User user = User.builder()
                .email(petition.getEmail())
                .baseSalary(BigDecimal.valueOf(3_000_000))
                .build();

        Petition approved1 = Petition.builder()
                .id("10")
                .amount(BigDecimal.valueOf(2_000_000))
                .term(24)
                .loanType(LoanTypeUtil.loanType())
                .status(StatusUtil.status())
                .build();

        Petition approved2 = Petition.builder()
                .id("11")
                .amount(BigDecimal.valueOf(3_000_000))
                .term(36)
                .loanType(LoanTypeUtil.loanType())
                .status(StatusUtil.status())
                .build();

        ValidationSqsMessage validationMessage =
                mapper.toValidationSqsMessage(petition, user, List.of(approved1, approved2));

        assertThat(validationMessage.getPetitionId()).isEqualTo(petition.getId());
        assertThat(validationMessage.getEmail()).isEqualTo(petition.getEmail());
        assertThat(validationMessage.getAmount()).isEqualTo(petition.getAmount());
        assertThat(validationMessage.getLoanTypeName()).isEqualTo(petition.getLoanType().getName());
        assertThat(validationMessage.getBaseSalary()).isEqualTo(user.getBaseSalary());
        assertThat(validationMessage.getInterestRate()).isEqualTo(petition.getLoanType().getInterestRate());
        assertThat(validationMessage.getApprovedLoans()).hasSize(2);

        ApprovedLoanDTO dto1 = validationMessage.getApprovedLoans().get(0);
        assertThat(dto1.getAmount()).isEqualTo(approved1.getAmount());
        assertThat(dto1.getTerm()).isEqualTo(approved1.getTerm());
        assertThat(dto1.getInterestRate()).isEqualTo(approved1.getLoanType().getInterestRate());
    }

    @Test
    void mustMapToReportSqsMessage() {
        Petition petition = PetitionUtil.petition();

        ReportSqsMessage message = mapper.toReportSqsMessage(petition);

        assertThat(message.getPetitionId()).isEqualTo(petition.getId());
        assertThat(message.getEmail()).isEqualTo(petition.getEmail());
        assertThat(message.getAmount()).isEqualTo(petition.getAmount());
    }
}
