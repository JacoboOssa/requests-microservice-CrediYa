package co.com.crediya.usecase.petition;

import co.com.crediya.model.dto.ListPetitionsDTO;
import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.notificationmessage.gateways.NotificationMessageRepository;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.status.gateways.StatusRepository;
import co.com.crediya.model.user.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.dto.PetitionSqsMessage;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RequiredArgsConstructor
public class PetitionUseCase {

    private final static String DEFAULT_PETITION_STATUS = "PENDIENTE";

    private final PetitionRepository petitionRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final NotificationMessageRepository notificationMessageRepository;

    public Mono<Petition> registerPetition(Petition petition, String identificationNumber, String token) {
        return validateAndGetUser(identificationNumber, token)
                .flatMap(user -> validateLoanTypeAndRange(petition)
                        .flatMap(loanType -> getPendingStatus()
                                .flatMap(status -> savePetition(petition, user, loanType, status))
                        )
                );
    }

    public Flux<ListPetitionsDTO> getAllPetitionsPaginable(List<String> statuses, int page, int size, String token) {
        return validateAndGetUserRole(token, Role.ROLE_ASESOR.name())
                .thenMany(fetchPetitionsWithDetails(statuses, page, size, token));
    }

    public Mono<Petition> updatePetition(Petition petition, String token) {
        return validateAndGetUserRole(token, Role.ROLE_ASESOR.name())
                .flatMap(user -> petitionRepository.findById(petition.getId())
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.PETITION_NOT_FOUND)))
                )
                .flatMap(existingPetition -> statusRepository.getStatusByName(petition.getStatus().getName())
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.STATUS_NOT_FOUND)))
                        .flatMap(status ->
                                petitionRepository.updatePetitionStatus(existingPetition.getId(), status.getId())
                                        .flatMap(updatedPetition -> loanTypeRepository.findById(updatedPetition.getLoanType().getId())
                                                .flatMap(loanType -> {
                                                    updatedPetition.setStatus(status);
                                                    updatedPetition.setLoanType(loanType);

                                                    // Mapear y enviar a SQS
                                                    PetitionSqsMessage sqsMessage = toSqsMessage(updatedPetition);
                                                    String messageJson = sqsMessage.toJson();

                                                    return notificationMessageRepository.sendRequestStatusNotification(messageJson)
                                                            .thenReturn(updatedPetition);
                                                })
                                        )
                        )
                );
    }




    private Mono<User> validateAndGetUser(String identificationNumber, String token) {
        return validateJwtToken(token)
                .flatMap(authUser -> validateRole(authUser, Role.ROLE_CLIENT.name())
                        .thenReturn(authUser))
                .flatMap(authUser -> fetchUserByIdentification(identificationNumber, token)
                        .flatMap(user -> validateEmailOwnership(authUser, user)
                                .thenReturn(user)
                        )
                );
    }

    private Mono<User> validateAndGetUserRole(String token, String requiredRole) {
        return validateJwtToken(token)
                .flatMap(user -> validateRole(user, requiredRole).thenReturn(user));
    }

    private Mono<User> validateJwtToken(String token) {
        String extractedToken = extractToken(token);
        return userRepository.validateJwtToken(extractedToken)
                .switchIfEmpty(Mono.error(new JwtException(JwtException.INVALID_TOKEN)));
    }

    private Mono<User> fetchUserByIdentification(String identificationNumber, String token) {
        return userRepository.findByIdentificationNumber(identificationNumber, token)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.USER_NOT_FOUND)));
    }

    private Mono<Void> validateRole(User user, String allowedRole) {
        if (user == null || user.getRol() == null || !user.getRol().equalsIgnoreCase(allowedRole)) {
            return Mono.error(new AuthorizationException(AuthorizationException.FORBIDDEN));
        }
        return Mono.empty();
    }

    private Mono<Void> validateEmailOwnership(User authUser, User identifiedUser) {
        if (!authUser.getEmail().equalsIgnoreCase(identifiedUser.getEmail())) {
            return Mono.error(new AuthorizationException(AuthorizationException.EMAIL_NOT_OWNER));
        }
        return Mono.empty();
    }

    private String extractToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new JwtException(JwtException.TOKEN_NOT_FOUND);
        }
        return token.substring(7);
    }


    private Mono<LoanType> validateLoanTypeAndRange(Petition petition) {
        return loanTypeRepository.findByName(petition.getLoanType().getName())
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.LOAN_TYPE_NOT_FOUND)))
                .flatMap(loanType -> validateLoanAmount(petition, loanType));
    }

    private Mono<LoanType> validateLoanAmount(Petition petition, LoanType loanType) {
        if (petition.getAmount().compareTo(loanType.getMinAmount()) < 0 ||
                petition.getAmount().compareTo(loanType.getMaxAmount()) > 0) {
            return Mono.error(new BusinessException(BusinessException.AMOUNT_OUT_OF_RANGE));
        }
        return Mono.just(loanType);
    }

    private Mono<Status> getPendingStatus() {
        return statusRepository.getStatusByName(DEFAULT_PETITION_STATUS);
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


    private Flux<ListPetitionsDTO> fetchPetitionsWithDetails(List<String> statuses, int page, int size, String token) {
        return petitionRepository.findPetitionsByStatus(statuses, page, size)
                .flatMap(petition -> Mono.zip(
                        statusRepository.findById(petition.getStatus().getId()),
                        loanTypeRepository.findById(petition.getLoanType().getId()),
                        userRepository.getAllUserInfoByEmail(petition.getEmail(), token),
                        petitionRepository.findApprovedByEmail(petition.getEmail()).collectList()
                ).map(tuple -> mapToDTO(petition, tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4())));
    }

    private ListPetitionsDTO mapToDTO(Petition petition, Status status, LoanType loanType, User user, List<Petition> approved) {
        BigDecimal totalMonthlyDebt = approved.stream()
                .map(p -> p.getAmount().divide(BigDecimal.valueOf(p.getTerm()), RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ListPetitionsDTO.builder()
                .id(petition.getId())
                .email(petition.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .baseSalary(user.getBaseSalary())
                .term(petition.getTerm())
                .amount(petition.getAmount())
                .loanTypeName(loanType.getName())
                .statusName(status.getName())
                .totalMonthlyDebtApproved(totalMonthlyDebt)
                .build();
    }

    private PetitionSqsMessage toSqsMessage(Petition petition) {
        return PetitionSqsMessage.builder()
                .petitionId(petition.getId())
                .email(petition.getEmail())
                .statusName(petition.getStatus().getName())
                .statusDescription(petition.getStatus().getDescription())
                .loanTypeName(petition.getLoanType().getName())
                .amount(petition.getAmount())
                .term(petition.getTerm())
                .build();
    }
}
