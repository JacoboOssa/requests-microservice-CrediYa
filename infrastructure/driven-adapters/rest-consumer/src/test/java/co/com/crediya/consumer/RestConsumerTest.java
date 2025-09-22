package co.com.crediya.consumer;


import co.com.crediya.consumer.config.RestConsumerPath;
import co.com.crediya.consumer.mapper.UserMapper;
import co.com.crediya.model.exception.AuthorizationException;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {"adapter.restconsumer.paths.find-by-identification-number=/api/v1/usuarios/id/{identificationNumber", "adapter.restconsumer.paths.validate-token=/auth/api/v1/validate/{jwt}","adapter.restconsumer.paths.get-all-user-info-by-email=/api/v1/usuarios/email/{email}"})
class RestConsumerTest {

    private static RestConsumer restConsumer;

    private static MockWebServer mockBackEnd;
    private static UserMapper userMapper;
    private static RestConsumerPath restConsumerPath;



    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        userMapper = Mockito.mock(UserMapper.class);
        mockBackEnd.start();
        restConsumerPath = new RestConsumerPath();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new RestConsumer(webClient, userMapper, restConsumerPath);
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

    @Test
    void mustReturnUnauthorizedException(){
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.FORBIDDEN.value()));

        StepVerifier.create(restConsumer.validateJwtToken("invalid-jwt-token"))
                .expectErrorMatches(error -> error instanceof AuthorizationException &&
                        error.getMessage().equals(AuthorizationException.UNAUTHORIZED))
                .verify();
    }

    @Test
    void mustReturnAllInfoFromUserGivenEmail(){
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"email\": \"jq@gmail.com\"}"));

        var expectedUser = new User();
        expectedUser.setEmail("jq@gmail.com");

        when(userMapper.toUserFromExtraInfo(any())).thenReturn(expectedUser);

        Mono<User> response = restConsumer.getAllUserInfoByEmail("jq@gmail.com", "validToken");

        StepVerifier.create(response)
                .expectNextMatches(user -> user.getEmail().equals("jq@gmail.com"))
                .verifyComplete();
    }

    @Test
    void mustReturnUserNotFoundWhenInvokeGetInfoUser(){
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.CONFLICT.value()));

        Mono<User> response = restConsumer.getAllUserInfoByEmail("jq@gmail.com", "validToken");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        error.getMessage().equals(BusinessException.USER_NOT_FOUND))
                .verify();
    }

    @Test
    void mustReturnUnauthorizedErrorWhenFindEmailGivenIdentificationNumber(){
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.UNAUTHORIZED.value()));

        var expectedUser = new User();
        expectedUser.setEmail("jq@gmail.com");

        Mono<User> response = restConsumer.findByIdentificationNumber("123456789", "validToken");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof JwtException &&
                        error.getMessage().equals(JwtException.INVALID_TOKEN))
                .verify();
    }

    @Test
    void mustReturnUnauthorizedErrorWhenFindEmailGivenEmail(){
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.UNAUTHORIZED.value()));


        Mono<User> response = restConsumer.getAllUserInfoByEmail("jq@gmail.com", "validToken");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof JwtException &&
                        error.getMessage().equals(JwtException.INVALID_TOKEN))
                .verify();
    }

    /*
    @Test
    void mustReturnForbiddenErrorWhenFindEmailGivenIdentificationNumber(){
        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.FORBIDDEN.value()));

        var expectedUser = new User();
        expectedUser.setEmail("jq@gmail.com");

        Mono<User> response = restConsumer.findByIdentificationNumber("123456789", "validToken");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof AuthorizationException &&
                        error.getMessage().equals(AuthorizationException.FORBIDDEN))
                .verify();
    }

    @Test
    void mustReturnForbiddenErrorWhenFindEmailGivenEmail(){
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.FORBIDDEN.value()));


        Mono<User> response = restConsumer.getAllUserInfoByEmail("jq@gmail.com", "validToken");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof AuthorizationException &&
                        error.getMessage().equals(AuthorizationException.FORBIDDEN))
                .verify();
    }
     */
}