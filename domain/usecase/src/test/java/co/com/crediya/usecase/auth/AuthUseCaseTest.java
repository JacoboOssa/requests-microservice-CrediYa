package co.com.crediya.usecase.auth;

import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthUseCase authUseCase;

    @Test
    void mustValidateAndGetUserSuccessfully() {
        String token = "Bearer validToken";
        String extracted = "validToken";
        String identificationNumber = "123";

        User user = new User();
        user.setEmail("test@test.com");
        user.setRol("ROLE_CLIENT");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(user));
        when(userRepository.findByIdentificationNumber(identificationNumber, token)).thenReturn(Mono.just(user));

        StepVerifier.create(authUseCase.validateAndGetUser(identificationNumber, token))
                .expectNextMatches(u -> u.getEmail().equals("test@test.com"))
                .verifyComplete();
    }

    @Test
    void mustFailWhenRoleIsNotClient() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User user = new User();
        user.setEmail("test@test.com");
        user.setRol("ROLE_ADMIN");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(user));

        StepVerifier.create(authUseCase.validateAndGetUser("123", token))
                .expectErrorMatches(e -> e instanceof AuthorizationException &&
                        e.getMessage().equals(AuthorizationException.FORBIDDEN))
                .verify();
    }

    @Test
    void mustFailWhenEmailsDoNotMatch() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User authUser = new User();
        authUser.setEmail("auth@test.com");
        authUser.setRol("ROLE_CLIENT");

        User identifiedUser = new User();
        identifiedUser.setEmail("other@test.com");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(authUser));
        when(userRepository.findByIdentificationNumber("123", token)).thenReturn(Mono.just(identifiedUser));

        StepVerifier.create(authUseCase.validateAndGetUser("123", token))
                .expectErrorMatches(e -> e instanceof AuthorizationException &&
                        e.getMessage().equals(AuthorizationException.EMAIL_NOT_OWNER))
                .verify();
    }

    @Test
    void mustFailWhenUserNotFoundByIdentification() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User authUser = new User();
        authUser.setEmail("auth@test.com");
        authUser.setRol("ROLE_CLIENT");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(authUser));
        when(userRepository.findByIdentificationNumber("123", token)).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.validateAndGetUser("123", token))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.USER_NOT_FOUND))
                .verify();
    }

    @Test
    void mustValidateAndGetUserRoleSuccessfully() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User user = new User();
        user.setRol("ROLE_ASESOR");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(user));

        StepVerifier.create(authUseCase.validateAndGetUserRole(token, "ROLE_ASESOR"))
                .expectNextMatches(u -> u.getRol().equals("ROLE_ASESOR"))
                .verifyComplete();
    }

    @Test
    void mustFailWhenRoleDoesNotMatch() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User user = new User();
        user.setRol("ROLE_CLIENT");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(user));

        StepVerifier.create(authUseCase.validateAndGetUserRole(token, "ROLE_ASESOR"))
                .expectErrorMatches(e -> e instanceof AuthorizationException &&
                        e.getMessage().equals(AuthorizationException.FORBIDDEN))
                .verify();
    }

    @Test
    void mustGetAllUserInfoByEmailSuccessfully() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User authUser = new User();
        authUser.setEmail("auth@test.com");
        authUser.setRol("ROLE_CLIENT");

        User targetUser = new User();
        targetUser.setEmail("target@test.com");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(authUser));
        when(userRepository.getAllUserInfoByEmail("target@test.com", token)).thenReturn(Mono.just(targetUser));

        StepVerifier.create(authUseCase.getAllUserInfoByEmail("target@test.com", token))
                .expectNextMatches(u -> u.getEmail().equals("target@test.com"))
                .verifyComplete();
    }

    @Test
    void mustFailWhenUserNotFoundByEmail() {
        String token = "Bearer validToken";
        String extracted = "validToken";

        User authUser = new User();
        authUser.setEmail("auth@test.com");
        authUser.setRol("ROLE_CLIENT");

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.just(authUser));
        when(userRepository.getAllUserInfoByEmail("missing@test.com", token)).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.getAllUserInfoByEmail("missing@test.com", token))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.USER_NOT_FOUND))
                .verify();
    }


    @Test
    void mustFailWhenJwtValidationFails() {
        String token = "Bearer badToken";
        String extracted = "badToken";

        when(userRepository.validateJwtToken(extracted)).thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.validateAndGetUser("123", token))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals(JwtException.INVALID_TOKEN))
                .verify();
    }
}
