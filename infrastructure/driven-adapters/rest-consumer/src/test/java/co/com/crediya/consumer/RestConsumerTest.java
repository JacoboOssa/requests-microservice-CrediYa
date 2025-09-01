package co.com.crediya.consumer;


import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.user.User;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.IOException;


class RestConsumerTest {

    private static RestConsumer restConsumer;

    private static MockWebServer mockBackEnd;


    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        restConsumer = new RestConsumer(webClient);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    @DisplayName("Validate the Authentication microservice return user email given the Identification Number")
    void mustRetrieveEmailByIdentificationNumber() {

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"email\": \"jq@gmail.com\"}"));

        Mono<User> response = restConsumer.findByIdentificationNumber("123456789");

        StepVerifier.create(response)
                .expectNextMatches(user -> user.getEmail().equals("jq@gmail.com"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Validate the Authentication microservice return conflict error when the Identification Number does not exist")
    void mustReturnErrorWhenIdentificationNumberDoesNotExist(){

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.CONFLICT.value()));

        Mono<User> response = restConsumer.findByIdentificationNumber("987654321");

        StepVerifier.create(response)
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        error.getMessage().contains(BusinessException.USER_NOT_FOUND))
                .verify();
    }
}