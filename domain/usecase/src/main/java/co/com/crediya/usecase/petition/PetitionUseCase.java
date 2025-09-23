package co.com.crediya.usecase.petition;

import co.com.crediya.model.dto.ListPetitionsDTO;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.user.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.auth.AuthUseCase;
import co.com.crediya.usecase.mapper.PetitionMapper;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import co.com.crediya.usecase.petitionvalidator.PetitionValidatorUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class PetitionUseCase {


    private final PetitionRepository petitionRepository;
    private final AuthUseCase authUseCase;
    private final PetitionValidatorUseCase petitionValidatorUseCase;
    private final PetitionMapper petitionMapper;
    private final PetitionMessagingUseCase petitionMessagingUseCase;


    public Mono<Petition> registerPetition(Petition petition, String identificationNumber, String token) {
        return authUseCase.validateAndGetUser(identificationNumber, token)
                .flatMap(user -> createAndSavePetition(petition, user)
                        .flatMap(savedPetition -> handleAutomaticValidationIfNeeded(savedPetition, token))
                );
    }

    private Mono<Petition> createAndSavePetition(Petition petition, User user) {
        return petitionValidatorUseCase.validateLoanTypeAndRange(petition)
                .flatMap(loanType -> petitionValidatorUseCase.getPendingStatus()
                        .flatMap(status -> savePetition(petition, user, loanType, status))
                );
    }

    private Mono<Petition> handleAutomaticValidationIfNeeded(Petition savedPetition, String token) {
        if (!savedPetition.getLoanType().isAutomaticValidation()) {
            return Mono.just(savedPetition);
        }

        return Mono.zip(
                authUseCase.getAllUserInfoByEmail(savedPetition.getEmail(), token),
                petitionRepository.findApprovedByEmail(savedPetition.getEmail())
                        .flatMap(p -> petitionValidatorUseCase.findById(p.getLoanType().getId())
                                .map(loanTypeApproved -> {
                                    p.setLoanType(loanTypeApproved);
                                    return p;
                                })
                        )
                        .collectList()
        ).flatMap(tuple -> {
            User fullUser = tuple.getT1();
            List<Petition> approvedPetitions = tuple.getT2();
            return petitionMessagingUseCase.enqueueForDebtCapacity(savedPetition, fullUser, approvedPetitions)
                    .thenReturn(savedPetition);
        });
    }


    private Mono<Petition> savePetition(Petition petition, User user, LoanType loanType, Status status) {
        petition.setEmail(user.getEmail());
        petition.setLoanType(loanType);
        petition.setStatus(status);
        return petitionRepository.savePetition(petition)
                .map(saved -> {
                    saved.setLoanType(loanType);
                    saved.setStatus(status);
                    return saved;
                });
    }

    public Flux<ListPetitionsDTO> getAllPetitionsPaginable(List<String> statuses, int page, int size, String token) {
        return authUseCase.validateAndGetUserRole(token, Role.ROLE_ASESOR.name())
                .thenMany(fetchPetitionsWithDetails(statuses, page, size, token));
    }

    private Flux<ListPetitionsDTO> fetchPetitionsWithDetails(List<String> statuses, int page, int size, String token) {
        return petitionRepository.findPetitionsByStatus(statuses, page, size)
                .flatMap(petition -> Mono.zip(
                        petitionValidatorUseCase.findStatusById(petition.getStatus().getId()),
                        petitionValidatorUseCase.findById(petition.getLoanType().getId()),
                        authUseCase.getAllUserInfoByEmail(petition.getEmail(), token),
                        petitionRepository.findApprovedByEmail(petition.getEmail()).collectList()
                ).map(tuple -> petitionMapper.mapToDTO(petition, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4())));
    }

}
