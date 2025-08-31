package co.com.crediya.r2dbc.petition;

import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.r2dbc.entity.PetitionEntity;
import co.com.crediya.r2dbc.helper.PetitionMapper;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

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
}
