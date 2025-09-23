package co.com.crediya.model.debtcapacity.gateways;

import co.com.crediya.model.dto.ValidationSqsMessage;
import reactor.core.publisher.Mono;

public interface DebtCapacityRepository {
    Mono<String> calculateDebtCapacity(ValidationSqsMessage message);
}
