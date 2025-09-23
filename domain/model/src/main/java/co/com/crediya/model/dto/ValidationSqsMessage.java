package co.com.crediya.model.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ValidationSqsMessage {
    private String petitionId;
    private String email;
    private String loanTypeName;
    private BigDecimal amount;
    private Integer term;
    private BigDecimal baseSalary;
    private Double interestRate;
    private List<ApprovedLoanDTO> approvedLoans;
}
