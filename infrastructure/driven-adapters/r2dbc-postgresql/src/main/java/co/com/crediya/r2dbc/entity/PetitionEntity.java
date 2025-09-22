package co.com.crediya.r2dbc.entity;

import org.springframework.data.annotation.Id;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;


@Table(name = "petitions")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PetitionEntity {
    @Id
    @Column("id")
    private String id;
    @Column("term")
    private int term;
    @Column("amount")
    private BigDecimal amount;
    @Column("email")
    private String email;
    @Column("status_id")
    private String statusId;
    @Column("loan_type_id")
    private String loanTypeId;
}
