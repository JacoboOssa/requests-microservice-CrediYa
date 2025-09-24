package co.com.crediya.usecase.petitionvalidator;

import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.status.gateways.StatusRepository;
import co.com.crediya.usecase.util.LoanTypeUtil;
import co.com.crediya.usecase.util.PetitionUtil;
import co.com.crediya.usecase.util.StatusUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetitionValidatorUseCaseTest {

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private StatusRepository statusRepository;

    @InjectMocks
    private PetitionValidatorUseCase petitionValidatorUseCase;

    @Test
    void mustValidateLoanTypeAndRangeSuccessfully() {
        Petition petition = PetitionUtil.petition();
        LoanType loanType = LoanTypeUtil.loanType();

        when(loanTypeRepository.findByName(petition.getLoanType().getName())).thenReturn(Mono.just(loanType));

        StepVerifier.create(petitionValidatorUseCase.validateLoanTypeAndRange(petition))
                .expectNext(loanType)
                .verifyComplete();
    }

    @Test
    void mustFailWhenLoanTypeNotFound() {
        Petition petition = PetitionUtil.petition();

        when(loanTypeRepository.findByName(petition.getLoanType().getName())).thenReturn(Mono.empty());

        StepVerifier.create(petitionValidatorUseCase.validateLoanTypeAndRange(petition))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.LOAN_TYPE_NOT_FOUND))
                .verify();
    }

    @Test
    void mustFailWhenAmountOutOfRange() {
        Petition petition = PetitionUtil.petition();
        petition.setAmount(BigDecimal.valueOf(10_000_000)); // fuera de rango
        LoanType loanType = LoanTypeUtil.loanType();

        when(loanTypeRepository.findByName(petition.getLoanType().getName())).thenReturn(Mono.just(loanType));

        StepVerifier.create(petitionValidatorUseCase.validateLoanTypeAndRange(petition))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.AMOUNT_OUT_OF_RANGE))
                .verify();
    }

    @Test
    void mustReturnPendingStatus() {
        Status status = StatusUtil.status();

        when(statusRepository.getStatusByName("PENDIENTE")).thenReturn(Mono.just(status));

        StepVerifier.create(petitionValidatorUseCase.getPendingStatus())
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void mustFindLoanTypeById() {
        LoanType loanType = LoanTypeUtil.loanType();

        when(loanTypeRepository.findById("1")).thenReturn(Mono.just(loanType));

        StepVerifier.create(petitionValidatorUseCase.findById("1"))
                .expectNext(loanType)
                .verifyComplete();
    }

    @Test
    void mustFailWhenLoanTypeByIdNotFound() {
        when(loanTypeRepository.findById("99")).thenReturn(Mono.empty());

        StepVerifier.create(petitionValidatorUseCase.findById("99"))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.LOAN_TYPE_NOT_FOUND))
                .verify();
    }

    @Test
    void mustFindStatusByName() {
        Status status = StatusUtil.status();

        when(statusRepository.getStatusByName("PENDIENTE")).thenReturn(Mono.just(status));

        StepVerifier.create(petitionValidatorUseCase.getStatusByName("PENDIENTE"))
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void mustFailWhenStatusByNameNotFound() {
        when(statusRepository.getStatusByName(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(petitionValidatorUseCase.getStatusByName("INVALIDO"))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.STATUS_NOT_FOUND))
                .verify();
    }

    @Test
    void mustFindStatusById() {
        Status status = StatusUtil.status();

        when(statusRepository.findById("1")).thenReturn(Mono.just(status));

        StepVerifier.create(petitionValidatorUseCase.findStatusById("1"))
                .expectNext(status)
                .verifyComplete();
    }

    @Test
    void mustFailWhenStatusByIdNotFound() {
        when(statusRepository.findById("999")).thenReturn(Mono.empty());

        StepVerifier.create(petitionValidatorUseCase.findStatusById("999"))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.STATUS_NOT_FOUND))
                .verify();
    }
}
