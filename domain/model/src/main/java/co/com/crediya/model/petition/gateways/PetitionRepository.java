package co.com.crediya.model.petition.gateways;

import co.com.crediya.model.petition.Petition;
import reactor.core.publisher.Mono;

public interface PetitionRepository {
    Mono<Petition> savePetition(Petition petition);
}
