package co.com.crediya.model.notificationmessage.gateways;

import reactor.core.publisher.Mono;

public interface NotificationMessageRepository {
    Mono<String> sendRequestStatusNotification(String message);
}
