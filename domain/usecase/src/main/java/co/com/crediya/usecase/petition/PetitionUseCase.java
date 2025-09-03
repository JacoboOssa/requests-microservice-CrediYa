package co.com.crediya.usecase.petition;

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
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class PetitionUseCase {
    private final PetitionRepository petitionRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;

    public Mono<Petition> registerPetition(Petition petition, String identificationNumber, String token) {
        final String extractedToken = extractToken(token);

        return userRepository.validateJwtToken(extractedToken)
                .switchIfEmpty(Mono.error(new JwtException(JwtException.INVALID_TOKEN)))
                .flatMap(authenticatedUser -> validateRole(authenticatedUser, Role.ROLE_CLIENT.name())
                        .thenReturn(authenticatedUser))
                .flatMap(authenticatedUser ->
                        validateUser(identificationNumber, extractedToken)
                                .flatMap(identifiedUser -> validateEmailOwner(authenticatedUser, identifiedUser.getEmail())
                                        .thenReturn(identifiedUser))
                )
                .flatMap(identifiedUser -> validateLoanType(petition)
                        .flatMap(loanType -> getPendingStatus()
                                .flatMap(status -> savePetition(petition, identifiedUser, loanType, status))
                        )
                );
    }


    private Mono<User> validateUser(String identificationNumber, String token) {
        return userRepository.findByIdentificationNumber(identificationNumber, token)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)));
    }

    private Mono<LoanType> validateLoanType(Petition petition) {
        return loanTypeRepository.findByName(petition.getLoanType().getName())
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.LOAN_TYPE_NOT_FOUND)))
                .flatMap(loanType -> {
                    if (petition.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                            petition.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
                        return Mono.error(new BusinessException(BusinessException.AMOUNT_OUT_OF_RANGE));
                    }
                    return Mono.just(loanType);
                });
    }

    private Mono<Status> getPendingStatus() {
        return statusRepository.getStatusByName("PENDIENTE");
    }

    private Mono<Petition> savePetition(Petition petition, User user, LoanType loanType, Status status) {
        petition.setEmail(user.getEmail());
        petition.setLoanType(loanType);
        petition.setStatus(status);
        return petitionRepository.savePetition(petition)
                .map(saved -> {
                    saved.setStatus(status);
                    saved.setLoanType(loanType);
                    return saved;
                });
    }

    private String extractToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new JwtException(JwtException.TOKEN_NOT_FOUND);
        }
        return token.substring(7);
    }



    private Mono<Void> validateRole(User user, String allowedRole) {
        if (user == null || user.getRol() == null || !user.getRol().equalsIgnoreCase(allowedRole)) {
            return Mono.error(new AuthorizationException(AuthorizationException.FORBIDDEN));
        }
        return Mono.empty();
    }

    private Mono<Void> validateEmailOwner(User user, String email) {
        if (user == null || user.getEmail() == null ||
                !user.getEmail().equalsIgnoreCase(email)) {
            return Mono.error(new AuthorizationException(AuthorizationException.EMAIL_NOT_OWNER));
        }
        return Mono.empty();
    }

}
