package co.com.crediya.model.user;
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
public class User {
    private String email;
    private String rol;
    private String name;
    private String lastName;
    private BigDecimal baseSalary;

    public static User fromJwt(String email, String rol) {
        return new User(email, rol, null, null, null);
    }

    public static User fromEmail(String email) {
        return new User(email, null, null, null, null);
    }

    public static User fromExtraInfo(String name, String lastName, BigDecimal baseSalary) {
        return new User(null, null, name, lastName, baseSalary);
    }
}
