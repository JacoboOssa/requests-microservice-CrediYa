package co.com.crediya.sqs.sender.config.automaticreview;

import co.com.crediya.model.dto.ValidationSqsMessage;
import co.com.crediya.sqs.sender.automaticreview.SQSSenderAutomaticQueue;
import co.com.crediya.sqs.sender.automaticreview.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSSenderAutomaticQueueTest {

    @Mock
    private SQSSenderProperties properties;

    @Mock
    private SqsAsyncClient client;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SQSSenderAutomaticQueue sender;

    @Test
    void mustSendMessageSuccessfully() throws Exception {
        // arrange
        String queueUrl = "http://localhost:4566/000000000000/my-queue";
        String message = "{\"petitionId\":\"123\"}";
        String messageId = "msg-123";

        when(properties.queueUrl()).thenReturn(queueUrl);

        CompletableFuture<SendMessageResponse> future =
                CompletableFuture.completedFuture(SendMessageResponse.builder().messageId(messageId).build());

        when(client.sendMessage(any(SendMessageRequest.class))).thenReturn(future);

        // act & assert
        StepVerifier.create(sender.send(message))
                .expectNext(messageId)
                .verifyComplete();

        verify(client, times(1)).sendMessage(argThat((SendMessageRequest req) ->
                req.queueUrl().equals(queueUrl) &&
                        req.messageBody().equals(message)
        ));
    }

    @Test
    void mustSerializeAndSendValidationMessage() throws Exception {
        // arrange
        ValidationSqsMessage validationMsg = new ValidationSqsMessage();
        validationMsg.setPetitionId("p1");
        validationMsg.setEmail("test@email.com");

        String serialized = "{\"petitionId\":\"p1\",\"email\":\"test@email.com\"}";

        when(objectMapper.writeValueAsString(any())).thenReturn(serialized);
        when(properties.queueUrl()).thenReturn("http://queue-url");

        CompletableFuture<SendMessageResponse> future =
                CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("m-1").build());

        when(client.sendMessage(any(SendMessageRequest.class))).thenReturn(future);

        // act & assert
        StepVerifier.create(sender.calculateDebtCapacity(validationMsg))
                .expectNext("m-1")
                .verifyComplete();

        verify(objectMapper).writeValueAsString(validationMsg);
    }
}

