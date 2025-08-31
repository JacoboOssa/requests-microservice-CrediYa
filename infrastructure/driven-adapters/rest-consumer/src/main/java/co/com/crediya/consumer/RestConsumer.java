package co.com.crediya.consumer;

import co.com.crediya.consumer.dto.AuthUserResponseDTO;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestConsumer implements UserRepository {
    private final WebClient client;

    @Override
    public Mono<User> findByIdentificationNumber(String identificationNumber) {
        return client.get()
                .uri("api/v1/usuarios/{identificationNumber}", identificationNumber)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), // lambda compatible con HttpStatusCode
                        response -> Mono.error(
                                new BusinessException(BusinessException.USER_NOT_FOUND + ": " + identificationNumber)
                        ))
                .bodyToMono(AuthUserResponseDTO.class)
                .map(userResponse -> new User(userResponse.email()))
                .doOnError(error -> log.error("Error fetching user with ID {}: {}", identificationNumber, error.getMessage()));
    }



}
