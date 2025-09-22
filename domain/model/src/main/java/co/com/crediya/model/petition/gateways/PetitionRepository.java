package co.com.crediya.model.petition.gateways;

import co.com.crediya.model.petition.Petition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PetitionRepository {
    Mono<Petition> savePetition(Petition petition);
    Flux<Petition> findPetitionsByStatus(List<String> requestedStatuses, int page, int size);
    Flux<Petition> findApprovedByEmail(String email);
    Mono<Petition> updatePetitionStatus(String petitionId, String status);
    Mono<Petition> findById(String petitionId);

}
