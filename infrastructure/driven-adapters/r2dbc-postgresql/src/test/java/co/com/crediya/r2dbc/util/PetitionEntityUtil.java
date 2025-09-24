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
    public static PetitionEntity petitionEntity2(){
        return PetitionEntity.builder()
                .id("2")
                .term(36)
                .amount(BigDecimal.valueOf(45000))
                .email("sduran@gmail.com")
                .statusId("1")
                .loanTypeId("2")
                .build();
    }

    public static PetitionEntity petitionEntity3(){
        return PetitionEntity.builder()
                .id("3")
                .term(24)
                .amount(BigDecimal.valueOf(5000000))
                .email("abogadesubrogador@gmail.com")
                .statusId("2")
                .loanTypeId("3")
                .build();
    }
}

