package co.com.crediya.r2dbc.loantype;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.r2dbc.entity.LoanTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanTypeReactiveRepository extends ReactiveCrudRepository<LoanTypeEntity, String>, ReactiveQueryByExampleExecutor<LoanTypeEntity> {
    Mono<LoanType> findByName(String name);

    boolean existsByName(String name);
}
