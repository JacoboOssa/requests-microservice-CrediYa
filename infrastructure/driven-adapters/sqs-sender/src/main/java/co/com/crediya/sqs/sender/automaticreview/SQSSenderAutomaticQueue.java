package co.com.crediya.sqs.sender.automaticreview;

import co.com.crediya.model.debtcapacity.gateways.DebtCapacityRepository;
import co.com.crediya.model.dto.ValidationSqsMessage;
import co.com.crediya.model.exception.AwsException;
import co.com.crediya.sqs.sender.automaticreview.config.SQSSenderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class SQSSenderAutomaticQueue implements DebtCapacityRepository {

    private static final String SERIALIZE_ERROR = "Error serializando mensaje a JSON";

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final ObjectMapper mapper;


    public Mono<String> send(String message) {
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Message sent {}", response.messageId()))
                .doOnError(throwable -> log.error("Error sending message", throwable))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }

    @Override
    public Mono<String> calculateDebtCapacity(ValidationSqsMessage message) {
        return Mono.fromCallable(() -> {
            try {
                return mapper.writeValueAsString(message);
            } catch (Exception e) {
                throw new AwsException(SERIALIZE_ERROR);
            }
        }).flatMap(this::send);
    }
}
