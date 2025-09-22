package co.com.crediya.model.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserFromJwt() {
        String email = "user@mail.com";
        String rol = "ADMIN";

        User user = User.fromJwt(email, rol);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRol()).isEqualTo(rol);
    }

    @Test
    void shouldCreateUserFromEmail() {
        String email = "onlyemail@mail.com";

        User user = User.fromEmail(email);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRol()).isNull();
    }

    @Test
    void shouldUseBuilderToModifyUser() {
        User original = User.fromJwt("user@mail.com", "USER");

        User modified = original.toBuilder()
                .rol("ADMIN")
                .build();

        assertThat(modified.getEmail()).isEqualTo("user@mail.com");
        assertThat(modified.getRol()).isEqualTo("ADMIN");
    }
}
