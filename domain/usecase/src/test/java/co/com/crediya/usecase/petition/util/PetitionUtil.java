package co.com.crediya.usecase.petition.util;

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

    public static Petition petition2() {
        return Petition.builder()
                .id("2")
                .term(24)
                .amount(BigDecimal.valueOf(5000000))
                .email("asesor@gmail.com")
                .status(StatusUtil.status())
                .loanType(LoanTypeUtil.loanType())
                .build();
    }
}
