package co.com.crediya.sqs.sender.config.reports;

import co.com.crediya.sqs.sender.reports.SQSSenderReports;
import co.com.crediya.sqs.sender.reports.config.SQSSenderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SQSSenderReportsTest {

    private SQSSenderProperties properties;
    private SqsAsyncClient client;
    private SQSSenderReports sender;

    private final String queueUrl = "http://localhost:4566/000000000000/reports-queue";

    @BeforeEach
    void setUp() {
        properties = mock(SQSSenderProperties.class);
        client = mock(SqsAsyncClient.class);
        sender = new SQSSenderReports(properties, client);

        when(properties.queueUrl()).thenReturn(queueUrl);
    }

    @Test
    void mustSendMessageSuccessfully() {
        String message = "report message";
        String messageId = "msg-123";

        // Mockear respuesta exitosa de SQS
        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(messageId)
                .build();
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        Mono<String> result = sender.addNewPetitionReport(message);

        StepVerifier.create(result)
                .expectNext(messageId)
                .verifyComplete();

        // Verificar que se construyó correctamente el request
        verify(client, times(1)).sendMessage(argThat((SendMessageRequest req) ->
                req.queueUrl().equals(queueUrl) &&
                        req.messageBody().equals(message)
        ));
    }

    @Test
    void mustHandleErrorWhenSendingMessage() {
        String message = "report message";

        // Mockear error al enviar
        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("SQS failure")));

        Mono<String> result = sender.addNewPetitionReport(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("SQS failure"))
                .verify();

        // Verificar que sí se intentó enviar
        verify(client).sendMessage(any(SendMessageRequest.class));
    }
}

