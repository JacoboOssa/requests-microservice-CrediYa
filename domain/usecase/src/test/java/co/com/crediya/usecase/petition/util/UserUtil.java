package co.com.crediya.usecase.petition.util;

import co.com.crediya.model.user.User;

import java.math.BigDecimal;

public class UserUtil {

    public static User user(){
        return User.builder()
                .email("jq@gmail.com")
                .rol("ROLE_CLIENT")
                .name("Juan")
                .lastName("Quintero")
                .baseSalary(BigDecimal.valueOf(1500000.0))
                .build();
    }

    public static User user2(){
        return User.builder()
                .email("asesor@gmail.com")
                .rol("ROLE_ASESOR")
                .name("Darwin")
                .lastName("Lenis")
                .baseSalary(BigDecimal.valueOf(1500000.0))
                .build();

    }
}
