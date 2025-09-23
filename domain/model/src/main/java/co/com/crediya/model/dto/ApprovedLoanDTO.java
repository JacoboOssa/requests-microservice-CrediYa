package co.com.crediya.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ApprovedLoanDTO {
    private BigDecimal amount;
    private int term;
    private Double interestRate;
}
