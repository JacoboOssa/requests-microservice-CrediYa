package co.com.crediya.usecase.petition;

import co.com.crediya.model.dto.ListPetitionsDTO;
import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.status.gateways.StatusRepository;
import co.com.crediya.model.user.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.auth.AuthUseCase;
import co.com.crediya.usecase.mapper.PetitionMapper;
import co.com.crediya.usecase.petition.util.LoanTypeUtil;
import co.com.crediya.usecase.petition.util.PetitionUtil;
import co.com.crediya.usecase.petition.util.StatusUtil;
import co.com.crediya.usecase.petition.util.UserUtil;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import co.com.crediya.usecase.petitionvalidator.PetitionValidatorUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetitionUseCaseTest {

    @Mock
    private PetitionRepository petitionRepository;

    @Mock
    private AuthUseCase authUseCase;

    @Mock
    private PetitionValidatorUseCase petitionValidatorUseCase;

    @Mock
    private PetitionMapper petitionMapper;

    @Mock
    private PetitionMessagingUseCase petitionMessagingUseCase;

    @InjectMocks
    private PetitionUseCase petitionUseCase;


    @Test
    void mustRegisterPetitionSuccessfully() {
        Petition petition = new Petition();
        petition.setLoanType(new LoanType());
        petition.setStatus(new Status());

        User user = new User();
        user.setEmail("test@test.com");

        LoanType loanType = new LoanType();
        loanType.setName("Personal Loan");
        loanType.setAutomaticValidation(false);

        Status status = new Status();
        status.setName("PENDIENTE");

        when(authUseCase.validateAndGetUser("123", "token")).thenReturn(Mono.just(user));
        when(petitionValidatorUseCase.validateLoanTypeAndRange(petition)).thenReturn(Mono.just(loanType));
        when(petitionValidatorUseCase.getPendingStatus()).thenReturn(Mono.just(status));
        when(petitionRepository.savePetition(any(Petition.class))).thenReturn(Mono.just(petition));

        StepVerifier.create(petitionUseCase.registerPetition(petition, "123", "token"))
                .expectNextMatches(saved ->
                        saved.getEmail().equals(user.getEmail()) &&
                                saved.getLoanType().getName().equals(loanType.getName()) &&
                                saved.getStatus().getName().equals(status.getName())
                )
                .verifyComplete();
    }

    @Test
    void mustRegisterPetitionWithAutomaticValidation() {
        Petition petition = new Petition();
        petition.setEmail("test@test.com");
        petition.setLoanType(new LoanType());
        petition.setStatus(new Status());

        User user = new User();
        user.setEmail("test@test.com");

        LoanType loanType = new LoanType();
        loanType.setName("Personal Loan");
        loanType.setAutomaticValidation(true);

        Status status = new Status();
        status.setName("PENDIENTE");

        when(authUseCase.validateAndGetUser("123", "token")).thenReturn(Mono.just(user));
        when(petitionValidatorUseCase.validateLoanTypeAndRange(petition)).thenReturn(Mono.just(loanType));
        when(petitionValidatorUseCase.getPendingStatus()).thenReturn(Mono.just(status));
        when(petitionRepository.savePetition(any(Petition.class))).thenReturn(Mono.just(petition));
        when(authUseCase.getAllUserInfoByEmail(petition.getEmail(), "token")).thenReturn(Mono.just(user));
        when(petitionRepository.findApprovedByEmail(petition.getEmail())).thenReturn(Flux.empty());
        when(petitionMessagingUseCase.enqueueForDebtCapacity(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(petitionUseCase.registerPetition(petition, "123", "token"))
                .expectNextMatches(saved -> saved.getEmail().equals("test@test.com"))
                .verifyComplete();
    }

    @Test
    void mustGetAllPetitionsPaginable() {
        Petition petition = new Petition();
        petition.setEmail("test@test.com");
        petition.setLoanType(new LoanType());
        petition.getLoanType().setId("1");
        petition.setStatus(new Status());
        petition.getStatus().setId("2");

        User user = new User();
        user.setEmail("test@test.com");

        LoanType loanType = new LoanType();
        loanType.setId("1");
        loanType.setName("Personal Loan");

        Status status = new Status();
        status.setId("2");
        status.setName("PENDIENTE");

        ListPetitionsDTO dto = new ListPetitionsDTO();
        dto.setLoanTypeName("Personal Loan");

        when(authUseCase.validateAndGetUserRole("token", Role.ROLE_ASESOR.name())).thenReturn(Mono.empty());
        when(petitionRepository.findPetitionsByStatus(List.of("PENDIENTE"), 1, 10)).thenReturn(Flux.just(petition));
        when(petitionValidatorUseCase.findStatusById("2")).thenReturn(Mono.just(status));
        when(petitionValidatorUseCase.findById("1")).thenReturn(Mono.just(loanType));
        when(authUseCase.getAllUserInfoByEmail("test@test.com", "token")).thenReturn(Mono.just(user));
        when(petitionRepository.findApprovedByEmail("test@test.com")).thenReturn(Flux.empty());
        when(petitionMapper.mapToDTO(petition, status, loanType, user, List.of())).thenReturn(dto);

        StepVerifier.create(petitionUseCase.getAllPetitionsPaginable(List.of("PENDIENTE"), 1, 10, "token"))
                .expectNextMatches(p -> p.getLoanTypeName().equals("Personal Loan"))
                .verifyComplete();
    }

}
