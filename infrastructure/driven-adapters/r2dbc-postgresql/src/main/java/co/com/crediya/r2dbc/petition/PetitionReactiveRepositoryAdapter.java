package co.com.crediya.r2dbc.petition;

import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.r2dbc.entity.PetitionEntity;
import co.com.crediya.r2dbc.helper.PetitionMapper;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class PetitionReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Petition/* change for domain model */,
        PetitionEntity/* change for adapter model */,
        String,
        PetitionReactiveRepository
> implements PetitionRepository {

    private final PetitionMapper petitionMapper;

    public PetitionReactiveRepositoryAdapter(PetitionReactiveRepository repository, ObjectMapper mapper, PetitionMapper petitionMapper) {
        super(repository, mapper, petitionMapper::toDomain);
        this.petitionMapper = petitionMapper;
    }

    @Override
    protected PetitionEntity toData(Petition petition) {
        return petitionMapper.toEntity(petition);
    }

    @Override
    public Mono<Petition> savePetition(Petition petition) {
        return save(petition);
    }

    @Override
    public Flux<Petition> findPetitionsByStatus(List<String> requestedStatuses, int page, int size) {

        List<String> allowedStatuses = List.of(
                "PENDIENTE",
                "RECHAZADA",
                "REVISION_MANUAL"
        );

        List<String> filteredStatuses;

        if (requestedStatuses == null || requestedStatuses.isEmpty()) {
            filteredStatuses = allowedStatuses;
        } else {
            filteredStatuses = requestedStatuses.stream()
                    .filter(allowedStatuses::contains)
                    .toList();
            if (filteredStatuses.isEmpty()) {
                return Flux.empty();
            }
        }

        return repository.findByStatuses(filteredStatuses)
                .skip(page * size)
                .take(size)
                .map(petitionMapper::toModel);
    }



    @Override
    public Flux<Petition> findApprovedByEmail(String email) {
        return repository.findApprovedByEmail("APROBADA")
                .map(petitionMapper::toDomain);
    }

    @Override
    public Mono<Petition> updatePetitionStatus(String petitionId, String statusId) {
        return repository.findById(petitionId)
                .flatMap(entity -> {
                    entity.setStatusId(statusId);
                    return repository.save(entity);
                })
                .map(petitionMapper::toModel);
    }


}
