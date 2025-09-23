package co.com.crediya.usecase.auth;

import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.user.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AuthUseCase {
    private final UserRepository userRepository;

    public Mono<User> validateAndGetUser(String identificationNumber, String token) {
        return validateJwtToken(token)
                .flatMap(authUser -> validateRole(authUser, Role.ROLE_CLIENT.name())
                        .thenReturn(authUser))
                .flatMap(authUser -> fetchUserByIdentification(identificationNumber, token)
                        .flatMap(user -> validateEmailOwnership(authUser, user).thenReturn(user)));
    }

    public Mono<User> validateAndGetUserRole(String token, String requiredRole) {
        return validateJwtToken(token)
                .flatMap(user -> validateRole(user, requiredRole).thenReturn(user));
    }

    public Mono<User> getAllUserInfoByEmail(String email, String token) {
        return validateJwtToken(token)
                .flatMap(authUser -> userRepository.getAllUserInfoByEmail(email, token)
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND))));
    }

    private Mono<User> validateJwtToken(String token) {
        String extractedToken = extractToken(token);
        return userRepository.validateJwtToken(extractedToken)
                .switchIfEmpty(Mono.error(new JwtException(JwtException.INVALID_TOKEN)));
    }

    private Mono<User> fetchUserByIdentification(String identificationNumber, String token) {
        return userRepository.findByIdentificationNumber(identificationNumber, token)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)));
    }

    private Mono<Void> validateRole(User user, String allowedRole) {
        if (user == null || user.getRol() == null || !user.getRol().equalsIgnoreCase(allowedRole)) {
            return Mono.error(new AuthorizationException(AuthorizationException.FORBIDDEN));
        }
        return Mono.empty();
    }

    private Mono<Void> validateEmailOwnership(User authUser, User identifiedUser) {
        if (!authUser.getEmail().equalsIgnoreCase(identifiedUser.getEmail())) {
            return Mono.error(new AuthorizationException(AuthorizationException.EMAIL_NOT_OWNER));
        }
        return Mono.empty();
    }

    private String extractToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new JwtException(JwtException.TOKEN_NOT_FOUND);
        }
        return token.substring(7);
    }
}
