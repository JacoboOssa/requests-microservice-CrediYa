package co.com.crediya.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "loan_types")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LoanTypeEntity {
    @Id
    @Column("id")
    private String id;
    @Column("name")
    private String name;
    @Column("min_amount")
    private BigDecimal minAmount;
    @Column("max_amount")
    private BigDecimal maxAmount;
    @Column("interest_rate")
    private Double interestRate;
    @Column("automatic_validation")
    private boolean automaticValidation;
}
