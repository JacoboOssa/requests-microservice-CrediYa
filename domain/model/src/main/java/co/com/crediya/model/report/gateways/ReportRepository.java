package co.com.crediya.model.report.gateways;

import reactor.core.publisher.Mono;

public interface ReportRepository {
    Mono<String> addNewPetitionReport(String message);
}
