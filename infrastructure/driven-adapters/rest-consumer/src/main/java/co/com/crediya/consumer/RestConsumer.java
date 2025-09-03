package co.com.crediya.consumer;

import co.com.crediya.consumer.dto.AuthUserResponseDTO;
import co.com.crediya.consumer.mapper.UserMapper;
import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRepository {
    private final WebClient client;
    private final UserMapper userMapper;

    @Override
    public Mono<User> findByIdentificationNumber(String identificationNumber, String token) {
        return client.get()
                .uri("api/v1/usuarios/{identificationNumber}", identificationNumber)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        response -> Mono.error(new JwtException(JwtException.INVALID_TOKEN)))
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
                        response -> Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)))
                .onStatus(status -> status.value() == HttpStatus.FORBIDDEN.value(),
                        response -> Mono.error(new AuthorizationException(AuthorizationException.UNAUTHORIZED)))
                .bodyToMono(AuthUserResponseDTO.class)
                .map(userMapper::toUserFromEmail) // ← Usando mapper, no `new`
                .doOnError(error ->
                        log.error("Error fetching user with ID {}: {}", identificationNumber, error.getMessage())
                );
    }


    @Override
    public Mono<User> validateJwtToken(String token) {
        return client.get()
                .uri("/auth/api/v1/validate/{jwt}", token)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        response -> Mono.error(new JwtException(JwtException.INVALID_TOKEN)))
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
                        response -> Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)))
                .onStatus(status -> status.value() == HttpStatus.FORBIDDEN.value(),
                        response -> Mono.error(new AuthorizationException(AuthorizationException.UNAUTHORIZED)))
                .bodyToMono(AuthUserResponseDTO.class)
                .map(userMapper::toUserFromJwt)
                .doOnError(error -> log.error("Error al validar JWT: {}", error.getMessage()));
    }

    @Override
    public Mono<User> getAllUserInfoByEmail(String email, String token) {
        return client.get()
                .uri("api/v1/usuarios/email/{email}", email)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        response -> Mono.error(new JwtException(JwtException.INVALID_TOKEN)))
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
                        response -> Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)))
                .onStatus(status -> status.value() == HttpStatus.FORBIDDEN.value(),
                        response -> Mono.error(new AuthorizationException(AuthorizationException.UNAUTHORIZED)))
                .bodyToMono(AuthUserResponseDTO.class)
                .map(userMapper::toUserFromExtraInfo)
                .doOnError(error ->
                        log.error("Error fetching user with email {}: {}", email, error.getMessage())
                );
    }


}
