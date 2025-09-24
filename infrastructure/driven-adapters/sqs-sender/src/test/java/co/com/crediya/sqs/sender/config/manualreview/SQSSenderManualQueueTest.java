package co.com.crediya.sqs.sender.config.manualreview;

import co.com.crediya.sqs.sender.manualreview.SQSSenderManualQueue;
import co.com.crediya.sqs.sender.manualreview.config.SQSSenderProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSSenderManualQueueTest {

    @Mock
    SqsAsyncClient client;

    @Mock
    SQSSenderProperties properties;

    @InjectMocks
    SQSSenderManualQueue sqsSenderManualQueue;

    @Test
    void mustSendMessageSuccessfully() {
        // arrange
        String queueUrl = "http://localhost:4566/000000000000/manual-queue";
        String message = "Hello Manual Review!";
        String messageId = "msg-123";

        when(properties.queueUrl()).thenReturn(queueUrl);

        SendMessageResponse response = SendMessageResponse.builder()
                .messageId(messageId)
                .build();

        when(client.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        // act & assert
        StepVerifier.create(sqsSenderManualQueue.sendRequestStatusNotification(message))
                .expectNext(messageId)
                .verifyComplete();

        // verify
        verify(client, times(1)).sendMessage(argThat((SendMessageRequest req) ->
                req.queueUrl().equals(queueUrl) &&
                        req.messageBody().equals(message)
        ));
    }
}

