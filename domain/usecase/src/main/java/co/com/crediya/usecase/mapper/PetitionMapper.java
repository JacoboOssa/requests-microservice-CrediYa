package co.com.crediya.usecase.mapper;

import co.com.crediya.model.dto.ApprovedLoanDTO;
import co.com.crediya.model.dto.ListPetitionsDTO;
import co.com.crediya.model.dto.ValidationSqsMessage;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.dto.PetitionSqsMessage;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
public class PetitionMapper {
    public ListPetitionsDTO mapToDTO(Petition petition, Status status, LoanType loanType, User user, List<Petition> approved) {
        BigDecimal totalMonthlyDebt = approved.stream()
                .map(p -> p.getAmount().divide(BigDecimal.valueOf(p.getTerm()), RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ListPetitionsDTO.builder()
                .id(petition.getId())
                .email(petition.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .baseSalary(user.getBaseSalary())
                .term(petition.getTerm())
                .amount(petition.getAmount())
                .loanTypeName(loanType.getName())
                .statusName(status.getName())
                .totalMonthlyDebtApproved(totalMonthlyDebt)
                .build();
    }

    public PetitionSqsMessage toSqsMessage(Petition petition) {
        return PetitionSqsMessage.builder()
                .petitionId(petition.getId())
                .email(petition.getEmail())
                .statusName(petition.getStatus().getName())
                .statusDescription(petition.getStatus().getDescription())
                .loanTypeName(petition.getLoanType().getName())
                .amount(petition.getAmount())
                .term(petition.getTerm())
                .build();
    }

    public ValidationSqsMessage toValidationSqsMessage(Petition petition, User user, List<Petition> approvedPetitions) {
        List<ApprovedLoanDTO> approvedLoans = approvedPetitions.stream()
                .map(this::toApprovedLoanDTO)
                .toList();

        return ValidationSqsMessage.builder()
                .petitionId(petition.getId())
                .email(petition.getEmail())
                .amount(petition.getAmount())
                .term(petition.getTerm())
                .loanTypeName(petition.getLoanType().getName())
                .baseSalary(user.getBaseSalary())
                .interestRate(petition.getLoanType().getInterestRate())
                .approvedLoans(approvedLoans)
                .build();
    }

    private ApprovedLoanDTO toApprovedLoanDTO(Petition petition) {
        return ApprovedLoanDTO.builder()
                .amount(petition.getAmount())
                .term(petition.getTerm())
                .interestRate(petition.getLoanType().getInterestRate())
                .build();
    }
}
