package co.com.crediya.sqs.listener;

import co.com.crediya.model.exception.AwsException;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SQSProcessorTest {

    @Mock
    private PetitionMessagingUseCase petitionMessagingUseCase;

    private ObjectMapper objectMapper;

    private SQSProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        processor = new SQSProcessor(petitionMessagingUseCase, objectMapper);
    }

    @Test
    void shouldProcessValidMessageSuccessfully() {
        // given
        Message message = Message.builder()
                .messageId("m1")
                .body("{\"petitionId\":\"123\",\"status\":\"APPROVED\"}")
                .build();

        when(petitionMessagingUseCase.updatePetitionStatusFromSQS(eq("123"), eq("APPROVED")))
                .thenReturn(Mono.empty());

        // when
        Mono<Void> result = processor.apply(message);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenJsonIsInvalid() {
        // given
        Message message = Message.builder()
                .messageId("m2")
                .body("not-a-json")
                .build();

        // when
        Mono<Void> result = processor.apply(message);

        // then
        StepVerifier.create(result)
                .expectError(AwsException.class)
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenUseCaseFails() {
        // given
        Message message = Message.builder()
                .messageId("m3")
                .body("{\"petitionId\":\"999\",\"status\":\"REJECTED\"}")
                .build();

        when(petitionMessagingUseCase.updatePetitionStatusFromSQS(eq("999"), eq("REJECTED")))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        // when
        Mono<Void> result = processor.apply(message);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof RuntimeException &&
                        err.getMessage().equals("DB error"))
                .verify();
    }
}
