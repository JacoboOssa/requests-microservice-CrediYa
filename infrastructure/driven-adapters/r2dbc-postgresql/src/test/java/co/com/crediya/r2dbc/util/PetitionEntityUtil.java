package co.com.crediya.r2dbc.util;

import co.com.crediya.r2dbc.entity.PetitionEntity;

import java.math.BigDecimal;

public class PetitionEntityUtil {
    public static PetitionEntity petitionEntity(){
        return PetitionEntity.builder()
                .id("1")
                .term(12)
                .amount(BigDecimal.valueOf(2460000))
                .email("jaco@gmail.com")
                .statusId("1")
                .loanTypeId("1")
                .build();
    }
}

