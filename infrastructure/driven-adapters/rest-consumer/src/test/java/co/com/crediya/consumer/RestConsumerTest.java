package co.com.crediya.consumer;


import co.com.crediya.consumer.mapper.UserMapper;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.user.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class RestConsumerTest {

    private static RestConsumer restConsumer;

    private static MockWebServer mockBackEnd;
    private static UserMapper userMapper;



    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        userMapper = Mockito.mock(UserMapper.class);
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new RestConsumer(webClient, userMapper);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    void mustRetrieveEmailByIdentificationNumber() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"email\": \"jq@gmail.com\"}"));

        var expectedUser = new User();
        expectedUser.setEmail("jq@gmail.com");

        when(userMapper.toUserFromEmail(any())).thenReturn(expectedUser);

        Mono<User> response = restConsumer.findByIdentificationNumber("123456789", "validToken");

        StepVerifier.create(response)
                .expectNextMatches(user -> user.getEmail().equals("jq@gmail.com"))
                .verifyComplete();
    }


    @Test
    @DisplayName("Validate the Authentication microservice return conflict error when the Identification Number does not exist")
    void mustReturnErrorWhenIdentificationNumberDoesNotExist(){

        String token = "valid-jwt-token";

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.CONFLICT.value()));

        Mono<User> response = restConsumer.findByIdentificationNumber("987654321", token);

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        error.getMessage().contains(BusinessException.USER_NOT_FOUND))
                .verify();
    }

    @Test
    @DisplayName("Should return user when JWT token is valid")
    void mustReturnUserWhenJwtIsValid() {
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"email\": \"valid@mail.com\", \"rol\": \"ADMIN\"}"));

        var expectedUser = new User();
        expectedUser.setEmail("valid@mail.com");
        expectedUser.setRol("ADMIN");

        when(userMapper.toUserFromJwt(any())).thenReturn(expectedUser);

        Mono<User> response = restConsumer.validateJwtToken("valid-jwt-token");

        StepVerifier.create(response)
                .expectNextMatches(user ->
                        user.getEmail().equals("valid@mail.com") &&
                                user.getRol().equals("ADMIN"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return error when JWT token is invalid")
    void mustReturnErrorWhenJwtIsInvalid() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.UNAUTHORIZED.value()));

        Mono<User> response = restConsumer.validateJwtToken("invalid-jwt-token");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof JwtException &&
                        error.getMessage().equals(JwtException.INVALID_TOKEN))
                .verify();
    }

    @Test
    @DisplayName("Should return error when user not found for JWT token")
    void mustReturnErrorWhenUserNotFoundForJwt() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CONFLICT.value()));

        Mono<User> response = restConsumer.validateJwtToken("jwt-without-user");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        error.getMessage().equals(BusinessException.USER_NOT_FOUND))
                .verify();
    }



}