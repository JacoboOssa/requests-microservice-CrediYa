package co.com.crediya.usecase.petitionvalidator;

import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.status.gateways.StatusRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class PetitionValidatorUseCase {
    private final static String DEFAULT_PETITION_STATUS = "PENDIENTE";

    private final LoanTypeRepository loanTypeRepository;
    private final StatusRepository statusRepository;


    public Mono<LoanType> validateLoanTypeAndRange(Petition petition) {
        return loanTypeRepository.findByName(petition.getLoanType().getName())
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.LOAN_TYPE_NOT_FOUND)))
                .flatMap(loanType -> validateLoanAmount(petition, loanType));
    }

    public Mono<LoanType> validateLoanAmount(Petition petition, LoanType loanType) {
        if (petition.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                petition.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
            return Mono.error(new BusinessException(BusinessException.AMOUNT_OUT_OF_RANGE));
        }
        return Mono.just(loanType);
    }

    public Mono<Status> getPendingStatus() {
        return statusRepository.getStatusByName(DEFAULT_PETITION_STATUS);
    }

    public Mono<LoanType> findById(String id) {
        return loanTypeRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.LOAN_TYPE_NOT_FOUND)));
    }

    public Mono<Status> getStatusByName(String name) {
        return statusRepository.getStatusByName(name)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.STATUS_NOT_FOUND)));
    }

    public Mono<Status> findStatusById(String id) {
        return statusRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.STATUS_NOT_FOUND)));
    }
}
