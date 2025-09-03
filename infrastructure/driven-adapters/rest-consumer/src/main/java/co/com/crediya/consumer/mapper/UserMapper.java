package co.com.crediya.consumer.mapper;

import co.com.crediya.consumer.dto.AuthUserResponseDTO;
import co.com.crediya.model.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toUserFromJwt(AuthUserResponseDTO dto) {
        return User.fromJwt(dto.email(), dto.rol());
    }

    public User toUserFromEmail(AuthUserResponseDTO dto) {
        return User.fromEmail(dto.email());
    }

    public User toUserFromExtraInfo(AuthUserResponseDTO dto) {
        return User.fromExtraInfo(dto.name(), dto.lastName(), dto.baseSalary());
    }
}

