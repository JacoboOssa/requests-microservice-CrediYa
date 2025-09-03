package co.com.crediya.usecase.petition;

import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.status.gateways.StatusRepository;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.petition.util.LoanTypeUtil;
import co.com.crediya.usecase.petition.util.PetitionUtil;
import co.com.crediya.usecase.petition.util.StatusUtil;
import co.com.crediya.usecase.petition.util.UserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetitionUseCaseTest {

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetitionRepository petitionRepository;

    @InjectMocks
    private PetitionUseCase petitionUseCase;


    @Test
    void mustValidateLoanType() {
        String loanTypeName = "Personal Loan";
        when(loanTypeRepository.findByName(loanTypeName)).thenReturn(Mono.just(LoanTypeUtil.loanType()));

        StepVerifier.create(loanTypeRepository.findByName(loanTypeName))
                .expectNextMatches(loanType -> loanType.getName().equals(loanTypeName) &&
                        loanType.getInterestRate().equals(5.5))
                .verifyComplete();
    }

    @Test
    void mustReturnErrorWhenLoanTypeNotFound() {
        String loanTypeName = "NonExistentLoanType";
        when(loanTypeRepository.findByName(loanTypeName)).thenReturn(Mono.empty());

        StepVerifier.create(loanTypeRepository.findByName(loanTypeName)
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.LOAN_TYPE_NOT_FOUND))))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(BusinessException.LOAN_TYPE_NOT_FOUND))
                .verify();
    }

    @Test
    void mustReturnErrorWhenAmountOutOfRange() {
        var petition = PetitionUtil.petition();
        petition.setAmount(BigDecimal.valueOf(1000000.0)); // Set an amount outside the valid range
        String loanTypeName = petition.getLoanType().getName();
        when(loanTypeRepository.findByName(loanTypeName)).thenReturn(Mono.just(LoanTypeUtil.loanType()));

        StepVerifier.create(loanTypeRepository.findByName(loanTypeName)
                        .flatMap(loanType -> {
                            if (petition.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                                    petition.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
                                return Mono.error(new BusinessException(BusinessException.AMOUNT_OUT_OF_RANGE));
                            }
                            return Mono.just(loanType);
                        }))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(BusinessException.AMOUNT_OUT_OF_RANGE))
                .verify();
    }


    @Test
    void mustValidateUser() {
        String identificationNumber = "123456789";
        String token = "validToken";
        when(userRepository.findByIdentificationNumber(identificationNumber, token)).thenReturn(Mono.just(UserUtil.user()));

        StepVerifier.create(userRepository.findByIdentificationNumber(identificationNumber, token))
                .expectNextMatches(user -> user.getEmail().equals("jq@gmail.com"))
                .verifyComplete();
    }

    @Test
    void mustReturnErrorWhenUserNotFound() {
        String identificationNumber = "987654321";
        String token = "validToken";
        when(userRepository.findByIdentificationNumber(identificationNumber, token)).thenReturn(Mono.empty());

        StepVerifier.create(userRepository.findByIdentificationNumber(identificationNumber, token)
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND))))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        throwable.getMessage().equals(BusinessException.USER_NOT_FOUND))
                .verify();
    }


    @Test
    void mustThrowForbiddenWhenRoleIsNotClient() {
        var petition = PetitionUtil.petition();
        var user = UserUtil.user();
        user.setRol("ROLE_ADMIN");
        String token = "Bearer validToken";

        when(userRepository.validateJwtToken("validToken")).thenReturn(Mono.just(user));

        StepVerifier.create(petitionUseCase.registerPetition(petition, "123456789", token))
                .expectErrorMatches(throwable -> throwable instanceof AuthorizationException &&
                        throwable.getMessage().equals(AuthorizationException.FORBIDDEN))
                .verify();
    }


    @Test
    void mustRegisterPetition() {
        String identificationNumber = "123456789";
        String token = "Bearer validToken";
        String extractedToken = "validToken";

        var petition = PetitionUtil.petition();
        var user = UserUtil.user();
        user.setRol("ROLE_CLIENT");

        var loanType = LoanTypeUtil.loanType();
        var status = StatusUtil.status();

        when(userRepository.validateJwtToken(extractedToken)).thenReturn(Mono.just(user));
        when(userRepository.findByIdentificationNumber(identificationNumber, extractedToken)).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(petition.getLoanType().getName())).thenReturn(Mono.just(loanType));
        when(statusRepository.getStatusByName("PENDIENTE")).thenReturn(Mono.just(status));
        when(petitionRepository.savePetition(petition)).thenReturn(Mono.just(petition));

        StepVerifier.create(petitionUseCase.registerPetition(petition, identificationNumber, token))
                .expectNextMatches(savedPetition ->
                        savedPetition.getEmail().equals(user.getEmail()) &&
                                savedPetition.getLoanType().getName().equals(loanType.getName()) &&
                                savedPetition.getStatus().getName().equals(status.getName()))
                .verifyComplete();
    }








}
