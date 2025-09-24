package co.com.crediya.sqs.listener.helper;

import co.com.crediya.sqs.listener.SQSProcessor;
import co.com.crediya.sqs.listener.config.SQSProperties;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SQSListenerTest {

    @Mock
    private PetitionMessagingUseCase petitionMessagingUseCase;

    private ObjectMapper objectMapper;

    @Mock
    private SqsAsyncClient asyncClient;

    private SQSProperties sqsProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sqsProperties = new SQSProperties(
                "us-east-2",
                "http://localhost:4566/queue/test-queue",
                10,  // maxNumberOfMessages
                20,  // waitTimeSeconds
                30,  // visibilityTimeout
                1    // numberOfThreads
        );

        objectMapper = new ObjectMapper();

        // Mock mensaje de SQS
        Message message = Message.builder()
                .messageId("m1")
                .body("{\"petitionId\":\"123\",\"status\":\"APPROVED\"}")
                .receiptHandle("receipt-handle")
                .build();

        var messageResponse = ReceiveMessageResponse.builder()
                .messages(message)
                .build();

        var deleteMessageResponse = DeleteMessageResponse.builder().build();

        when(asyncClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(messageResponse));

        when(asyncClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(deleteMessageResponse));

        when(petitionMessagingUseCase.updatePetitionStatusFromSQS("123", "APPROVED"))
                .thenReturn(Mono.empty());
    }

}
