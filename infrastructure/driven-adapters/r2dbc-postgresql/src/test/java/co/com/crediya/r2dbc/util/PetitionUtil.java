package co.com.crediya.r2dbc.util;

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
}
