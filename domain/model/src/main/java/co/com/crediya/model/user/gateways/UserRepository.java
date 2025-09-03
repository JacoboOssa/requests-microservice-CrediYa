package co.com.crediya.model.user.gateways;

import co.com.crediya.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> findByIdentificationNumber(String identificationNumber, String token);
    Mono<User> validateJwtToken(String token);
}
