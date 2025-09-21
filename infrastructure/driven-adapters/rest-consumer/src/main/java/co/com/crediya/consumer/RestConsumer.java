package co.com.crediya.consumer;

import co.com.crediya.consumer.config.RestConsumerPath;
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

    private static final String BEARER = "Bearer ";

    private final WebClient client;
    private final UserMapper userMapper;
    private final RestConsumerPath restConsumerPath;

    @Override
    public Mono<User> findByIdentificationNumber(String identificationNumber, String token) {
        return client.get()
                .uri(restConsumerPath.getFindByIdentificationNumber(), identificationNumber)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
                        response -> Mono.error(new JwtException(JwtException.INVALID_TOKEN)))
                .onStatus(status -> status.value() == HttpStatus.CONFLICT.value(),
                        response -> Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)))
                .onStatus(status -> status.value() == HttpStatus.FORBIDDEN.value(),
                        response -> Mono.error(new AuthorizationException(AuthorizationException.UNAUTHORIZED)))
                .bodyToMono(AuthUserResponseDTO.class)
                .map(userMapper::toUserFromEmail)
                .doOnError(error ->
                        log.error("Error fetching user with ID {}: {}", identificationNumber, error.getMessage())
                );
    }


    @Override
    public Mono<User> validateJwtToken(String token) {
        return client.get()
                .uri(restConsumerPath.getValidateToken(), token)
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
                .uri(restConsumerPath.getGetAllUserInfoByEmail(), email)
                .header(HttpHeaders.AUTHORIZATION, BEARER + token)
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
