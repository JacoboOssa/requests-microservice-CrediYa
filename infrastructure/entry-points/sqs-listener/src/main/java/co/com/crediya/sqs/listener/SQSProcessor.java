package co.com.crediya.sqs.listener;

import co.com.crediya.model.exception.AwsException;
import co.com.crediya.usecase.petition.PetitionUseCase;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final PetitionMessagingUseCase petitionMessagingUseCase;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("Mensaje recibido de SQS. MessageId={}, Body={}", message.messageId(), message.body());
        try {
            Map<String, Object> body = objectMapper.readValue(message.body(), Map.class);
            String petitionId = (String) body.get("petitionId");
            String status = (String) body.get("status");

            log.debug("Procesando petición {} con estado {}", petitionId, status);

            return petitionMessagingUseCase.updatePetitionStatusFromSQS(petitionId, status)
                    .doOnSuccess(v -> log.info("Estado de petición {} actualizado a {}", petitionId, status))
                    .doOnError(e -> log.error("Error actualizando petición {}: {}", petitionId, e.getMessage(), e));

        } catch (Exception e) {
            return Mono.error(new AwsException(AwsException.QUEUE_SEND_ERROR));
        }
    }
}
