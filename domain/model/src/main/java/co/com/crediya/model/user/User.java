package co.com.crediya.model.user;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private String email;
    private String rol;

    public static User fromJwt(String email, String rol) {
        return new User(email, rol);
    }

    public static User fromEmail(String email) {
        return new User(email, null);
    }
}
