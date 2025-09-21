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

    @Mock
    PetitionMapper petitionEntityMapper;

    @Mock
    PetitionMapper petitionMapper;


    @Test
    void mustSavePetition() {
        when(petitionEntityMapper.toEntity(any(Petition.class))).thenReturn(PetitionEntityUtil.petitionEntity());
        when(petitionEntityMapper.toDomain(any(PetitionEntity.class))).thenReturn(PetitionUtil.petition());

        when(repository.save(any(PetitionEntity.class))).thenReturn(Mono.just(PetitionEntityUtil.petitionEntity()));

        Mono<Petition> petitionMono = repositoryAdapter.savePetition(PetitionUtil.petition());

        StepVerifier.create(petitionMono)
                .expectNextMatches(petition -> petition.getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void mustRetrieveApprovedPetitionsForUser(){
        when(petitionMapper.toDomain(any(PetitionEntity.class))).thenReturn(PetitionUtil.petition());

        when(repository.findApprovedByEmail(any(String.class))).thenReturn(Flux.just(PetitionEntityUtil.petitionEntity()));

        StepVerifier.create(repositoryAdapter.findApprovedByEmail(PetitionEntityUtil.petitionEntity().getEmail()))
                .expectNextMatches(petition -> petition.getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void mustRetrieveAllNoApprovedPetitions(){

        List<String> statuses = List.of("PENDIENTE");

        when(petitionMapper.toModel(any(PetitionEntity.class))).thenReturn(PetitionUtil.petition());

        when(repository.findByStatuses(statuses)).thenReturn(Flux.just(PetitionEntityUtil.petitionEntity()));

        StepVerifier.create(repositoryAdapter.findPetitionsByStatus(statuses,0,20))
                .expectNextMatches(petition -> petition.getId().equals("1"))
                .verifyComplete();

    }


}
