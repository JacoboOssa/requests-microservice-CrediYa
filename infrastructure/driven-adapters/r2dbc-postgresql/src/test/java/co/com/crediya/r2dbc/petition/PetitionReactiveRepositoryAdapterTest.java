package co.com.crediya.r2dbc.petition;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
import co.com.crediya.r2dbc.entity.PetitionEntity;
import co.com.crediya.r2dbc.helper.PetitionMapper;
import co.com.crediya.r2dbc.util.PetitionEntityUtil;
import co.com.crediya.r2dbc.util.PetitionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;


import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetitionReactiveRepositoryAdapterTest {

    @InjectMocks
    PetitionReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    PetitionReactiveRepository repository;

//    @Mock
//    PetitionMapper petitionEntityMapper;

    @Mock
    PetitionMapper petitionMapper;


    @Test
    void mustSavePetition() {
        when(petitionMapper.toEntity(any(Petition.class)))
                .thenReturn(PetitionEntityUtil.petitionEntity());

        when(repository.save(any(PetitionEntity.class)))
                .thenReturn(Mono.just(PetitionEntityUtil.petitionEntity()));

        when(petitionMapper.toDomain(any(PetitionEntity.class)))
                .thenReturn(PetitionUtil.petition());

        Mono<Petition> petitionMono = repositoryAdapter.savePetition(PetitionUtil.petition());

        StepVerifier.create(petitionMono)
                .expectNextMatches(p -> p.getId().equals("1"))
                .verifyComplete();
    }


    @Test
    void mustRetrieveApprovedPetitionsForUser() {
        when(petitionMapper.toModel(any(PetitionEntity.class)))
                .thenReturn(PetitionUtil.petition());

        when(repository.findApprovedByEmail(any(String.class)))
                .thenReturn(Flux.just(PetitionEntityUtil.petitionEntity3()));

        StepVerifier.create(repositoryAdapter.findApprovedByEmail(PetitionEntityUtil.petitionEntity().getEmail()))
                .expectNextMatches(petition -> petition.getId().equals("1"))
                .verifyComplete();
    }


    @Test
    void mustRetrieveAllNoApprovedPetitions() {
        List<String> statuses = List.of("PENDIENTE");

        when(repository.findByStatuses(statuses))
                .thenReturn(Flux.just(PetitionEntityUtil.petitionEntity()));

        when(petitionMapper.toModel(any(PetitionEntity.class)))
                .thenReturn(PetitionUtil.petition());

        StepVerifier.create(repositoryAdapter.findPetitionsByStatus(statuses, 0, 20))
                .expectNextMatches(petition -> petition.getId().equals("1"))
                .verifyComplete();
    }


    @Test
    void mustUpdatePetitionStatus() {
        PetitionEntity entity = PetitionEntityUtil.petitionEntity();

        when(repository.findById("1")).thenReturn(Mono.just(entity));
        when(repository.save(any(PetitionEntity.class))).thenReturn(Mono.just(entity));
        when(petitionMapper.toModel(any(PetitionEntity.class))).thenReturn(PetitionUtil.petition());

        StepVerifier.create(repositoryAdapter.updatePetitionStatus("1", "2"))
                .expectNextMatches(p -> p.getId().equals("1"))
                .verifyComplete();
    }


    @Test
    void mustReturnAllowedStatusesWhenRequestedStatusesEmpty() {
        when(petitionMapper.toModel(any(PetitionEntity.class)))
                .thenReturn(PetitionUtil.petition());

        when(repository.findByStatuses(any()))
                .thenReturn(Flux.just(PetitionEntityUtil.petitionEntity()));

        StepVerifier.create(repositoryAdapter.findPetitionsByStatus(List.of(), 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void mustReturnEmptyWhenRequestedStatusesInvalid() {
        StepVerifier.create(repositoryAdapter.findPetitionsByStatus(List.of("INVALID"), 0, 10))
                .verifyComplete(); // no emite nada
    }





}
