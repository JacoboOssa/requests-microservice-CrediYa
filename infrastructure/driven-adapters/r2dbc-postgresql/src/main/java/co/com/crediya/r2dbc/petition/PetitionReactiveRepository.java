package co.com.crediya.r2dbc.petition;

import co.com.crediya.r2dbc.entity.PetitionEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PetitionReactiveRepository extends ReactiveCrudRepository<PetitionEntity, String>, ReactiveQueryByExampleExecutor<PetitionEntity> {

}
