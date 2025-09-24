package co.com.crediya.usecase.petitionmessaging;

import co.com.crediya.model.debtcapacity.gateways.DebtCapacityRepository;
import co.com.crediya.model.dto.ValidationSqsMessage;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.notificationmessage.gateways.NotificationMessageRepository;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.report.gateways.ReportRepository;
import co.com.crediya.model.user.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.usecase.auth.AuthUseCase;
import co.com.crediya.usecase.dto.PetitionSqsMessage;
import co.com.crediya.usecase.dto.ReportSqsMessage;
import co.com.crediya.usecase.mapper.PetitionMapper;
import co.com.crediya.usecase.petitionvalidator.PetitionValidatorUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class PetitionMessagingUseCase {

    private final PetitionRepository petitionRepository;
    private final PetitionValidatorUseCase petitionValidatorUseCase;
    private final AuthUseCase authUseCase;
    private final PetitionMapper petitionMapper;
    private final NotificationMessageRepository notificationMessageRepository;
    private final DebtCapacityRepository debtCapacityRepository;
    private final ReportRepository reportRepository;


    public Mono<Void> updatePetitionStatusFromSQS(String petitionId, String statusName) {
        return petitionRepository.findById(petitionId)
                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.PETITION_NOT_FOUND)))
                .flatMap(existingPetition ->
                        petitionValidatorUseCase.getStatusByName(statusName)
                                .switchIfEmpty(Mono.error(new BusinessException(BusinessException.STATUS_NOT_FOUND)))
                                .flatMap(status ->
                                        petitionRepository.updatePetitionStatus(existingPetition.getId(), status.getId())
                                                .then(Mono.defer(() -> {
                                                    if ("APROBADA".equalsIgnoreCase(statusName)) {
                                                        ReportSqsMessage sqsMessage = petitionMapper.toReportSqsMessage(existingPetition);

                                                        String messageJson = sqsMessage.toJson();
                                                        return reportRepository.addNewPetitionReport(messageJson).then();
                                                    }
                                                    return Mono.empty();
                                                }))
                                )
                );
    }


    public Mono<Petition> updatePetition(Petition petition, String token) {
        return authUseCase.validateAndGetUserRole(token, Role.ROLE_ASESOR.name())
                .flatMap(user -> petitionRepository.findById(petition.getId())
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.PETITION_NOT_FOUND)))
                )
                .flatMap(existingPetition -> petitionValidatorUseCase.getStatusByName(petition.getStatus().getName())
                        .switchIfEmpty(Mono.error(new BusinessException(BusinessException.STATUS_NOT_FOUND)))
                        .flatMap(status ->
                                petitionRepository.updatePetitionStatus(existingPetition.getId(), status.getId())
                                        .flatMap(updatedPetition -> petitionValidatorUseCase.findById(updatedPetition.getLoanType().getId())
                                                .flatMap(loanType -> {
                                                    updatedPetition.setStatus(status);
                                                    updatedPetition.setLoanType(loanType);

                                                    PetitionSqsMessage notificationSqsMessage = petitionMapper.toSqsMessage(updatedPetition);
                                                    String notificationMessageJson = notificationSqsMessage.toJson();

                                                    ReportSqsMessage reportSqsMessage = petitionMapper.toReportSqsMessage(updatedPetition);
                                                    String reportSqsMessageJson = reportSqsMessage.toJson();

                                                    return notificationMessageRepository.sendRequestStatusNotification(notificationMessageJson)
                                                            .then(Mono.defer(() -> {
                                                                if ("APROBADA".equalsIgnoreCase(status.getName())) {
                                                                    return reportRepository.addNewPetitionReport(reportSqsMessageJson).then();
                                                                }
                                                                return Mono.empty();
                                                            }))
                                                            .thenReturn(updatedPetition);

                                                })
                                        )
                        )
                );
    }

    public Mono<String> enqueueForDebtCapacity(Petition petition, User user, List<Petition> approvedPetitions) {
        ValidationSqsMessage message = petitionMapper.toValidationSqsMessage(petition, user, approvedPetitions);
        return debtCapacityRepository.calculateDebtCapacity(message);
    }

}
