package co.com.crediya.r2dbc.status;

import co.com.crediya.model.status.Status;
import co.com.crediya.model.status.gateways.StatusRepository;
import co.com.crediya.r2dbc.entity.StatusEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class StatusReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Status,
        StatusEntity,
        String,
        StatusReactiveRepository> implements StatusRepository {
    public StatusReactiveRepositoryAdapter(StatusReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.map(d, Status.class));
    }

    @Override
    public Mono<Status> getStatusByName(String name) {
        return repository.getStatusByName(name);
    }
}
