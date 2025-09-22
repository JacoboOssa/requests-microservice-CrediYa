package co.com.crediya.r2dbc.petition;

import co.com.crediya.r2dbc.entity.PetitionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface PetitionReactiveRepository extends ReactiveCrudRepository<PetitionEntity, String>, ReactiveQueryByExampleExecutor<PetitionEntity> {
    @Query("""
    SELECT p.* 
    FROM petitions p
    JOIN status s ON p.status_id = s.id
    WHERE s.name IN (:statuses)
    """)
    Flux<PetitionEntity> findByStatuses(List<String> statuses);
    @Query("""
        SELECT p.*
        FROM petitions p
        JOIN status s ON p.status_id = s.id
        WHERE p.email = :email AND s.name = 'APROBADA'
    """)
    Flux<PetitionEntity> findApprovedByEmail(String email);}
