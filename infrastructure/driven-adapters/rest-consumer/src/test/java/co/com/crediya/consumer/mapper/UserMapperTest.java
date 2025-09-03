package co.com.crediya.consumer.mapper;

import co.com.crediya.consumer.dto.AuthUserResponseDTO;
import co.com.crediya.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void shouldMapToUserFromJwt() {
        AuthUserResponseDTO dto = new AuthUserResponseDTO("test@mail.com", "ADMIN",null,null,null);

        User user = userMapper.toUserFromJwt(dto);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@mail.com");
        assertThat(user.getRol()).isEqualTo("ADMIN");
    }

    @Test
    void shouldMapToUserFromEmail() {
        AuthUserResponseDTO dto = new AuthUserResponseDTO("emailonly@mail.com", "USER_SHOULD_BE_IGNORED",null,null,null);

        User user = userMapper.toUserFromEmail(dto);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("emailonly@mail.com");
        assertThat(user.getRol()).isNull();
    }

    @Test
    void shouldMapToUserFromExtraInfo() {
        AuthUserResponseDTO dto = new AuthUserResponseDTO(null, null,"John","Doe",50000.0);

        User user = userMapper.toUserFromExtraInfo(dto);

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getBaseSalary()).isEqualTo(50000.0);
    }
}
