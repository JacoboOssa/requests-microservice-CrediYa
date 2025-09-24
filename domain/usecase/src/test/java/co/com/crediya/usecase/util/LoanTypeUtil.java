package co.com.crediya.usecase.util;

import co.com.crediya.model.loantype.LoanType;

import java.math.BigDecimal;

public class LoanTypeUtil {

    public static LoanType loanType(){
        return LoanType.builder()
                .id("1")
                .name("Personal Loan")
                .minAmount(BigDecimal.valueOf(2000000))
                .maxAmount(BigDecimal.valueOf(6000000))
                .automaticValidation(false)
                .interestRate(5.5)
                .build();
    }
}
