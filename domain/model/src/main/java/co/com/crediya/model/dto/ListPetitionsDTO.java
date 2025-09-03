package co.com.crediya.model.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ListPetitionsDTO {
    private String id;
    private String email;
    private String name;
    private String lastName;
    private Double baseSalary;
    private int term;
    private BigDecimal amount;
    private String loanTypeName;
    private String statusName;
    private BigDecimal totalMonthlyDebtApproved;
}
