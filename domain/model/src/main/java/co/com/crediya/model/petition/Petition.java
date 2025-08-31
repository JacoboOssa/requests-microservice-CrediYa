package co.com.crediya.model.petition;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.status.Status;
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
public class Petition {
    private String id;
    private int term;
    private BigDecimal amount;
    private String email;
    private Status status;
    private LoanType loanType;
}
