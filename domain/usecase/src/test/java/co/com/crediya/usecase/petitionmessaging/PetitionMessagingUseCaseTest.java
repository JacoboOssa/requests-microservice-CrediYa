package co.com.crediya.usecase.petitionmessaging;

import co.com.crediya.model.dto.ValidationSqsMessage;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.notificationmessage.gateways.NotificationMessageRepository;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.report.gateways.ReportRepository;
import co.com.crediya.model.status.Status;
import co.com.crediya.model.user.Role;
import co.com.crediya.model.user.User;
import co.com.crediya.model.debtcapacity.gateways.DebtCapacityRepository;
import co.com.crediya.usecase.auth.AuthUseCase;
import co.com.crediya.usecase.dto.PetitionSqsMessage;
import co.com.crediya.usecase.dto.ReportSqsMessage;
import co.com.crediya.usecase.mapper.PetitionMapper;
import co.com.crediya.usecase.util.LoanTypeUtil;
import co.com.crediya.usecase.util.PetitionUtil;
import co.com.crediya.usecase.util.StatusUtil;
import co.com.crediya.usecase.util.UserUtil;
import co.com.crediya.usecase.petitionvalidator.PetitionValidatorUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetitionMessagingUseCaseTest {

    @Mock
    private PetitionRepository petitionRepository;
    @Mock
    private PetitionValidatorUseCase petitionValidatorUseCase;
    @Mock
    private AuthUseCase authUseCase;
    @Mock
    private PetitionMapper petitionMapper;
    @Mock
    private NotificationMessageRepository notificationMessageRepository;
    @Mock
    private DebtCapacityRepository debtCapacityRepository;
    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private PetitionMessagingUseCase petitionMessagingUseCase;

    @Test
    void mustUpdatePetitionStatusFromSqsWithApprovedStatus() {
        Petition petition = new Petition();
        petition.setId("1");

        Status status = StatusUtil.status();
        status.setId("2");
        status.setName("APROBADA");

        PetitionSqsMessage petitionMessage = new PetitionSqsMessage();
        ReportSqsMessage reportMessage = new ReportSqsMessage();

        when(petitionRepository.findById("1")).thenReturn(Mono.just(petition));
        when(petitionValidatorUseCase.getStatusByName("APROBADA")).thenReturn(Mono.just(status));
        when(petitionRepository.updatePetitionStatus("1", "2")).thenReturn(Mono.empty());
        when(petitionMapper.toReportSqsMessage(petition)).thenReturn(reportMessage);
        when(reportRepository.addNewPetitionReport(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(petitionMessagingUseCase.updatePetitionStatusFromSQS("1", "APROBADA"))
                .verifyComplete();
    }

    @Test
    void mustFailWhenPetitionNotFoundInUpdateFromSqs() {
        when(petitionRepository.findById("1")).thenReturn(Mono.empty());

        StepVerifier.create(petitionMessagingUseCase.updatePetitionStatusFromSQS("1", "APROBADA"))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.PETITION_NOT_FOUND))
                .verify();
    }

    @Test
    void mustFailWhenStatusNotFoundInUpdateFromSqs() {
        Petition petition = new Petition();
        petition.setId("1");

        when(petitionRepository.findById("1")).thenReturn(Mono.just(petition));
        when(petitionValidatorUseCase.getStatusByName("INVALID")).thenReturn(Mono.empty());

        StepVerifier.create(petitionMessagingUseCase.updatePetitionStatusFromSQS("1", "INVALID"))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.STATUS_NOT_FOUND))
                .verify();
    }

    @Test
    void mustUpdatePetitionSuccessfully() {
        String token = "Bearer token";

        Petition petition = new Petition();
        petition.setId("1");
        petition.setStatus(StatusUtil.status());

        Petition existing = new Petition();
        existing.setId("1");
        existing.setLoanType(LoanTypeUtil.loanType());

        Status status = StatusUtil.status();
        status.setId("2");
        status.setName("APROBADA");

        Petition updated = new Petition();
        updated.setId("1");
        updated.setLoanType(existing.getLoanType());
        updated.setStatus(status);

        PetitionSqsMessage petitionMessage = new PetitionSqsMessage();
        ReportSqsMessage reportMessage = new ReportSqsMessage();

        when(authUseCase.validateAndGetUserRole(token, Role.ROLE_ASESOR.name()))
                .thenReturn(Mono.just(new User()));
        when(petitionRepository.findById("1")).thenReturn(Mono.just(existing));
        when(petitionValidatorUseCase.getStatusByName("PENDIENTE")).thenReturn(Mono.just(status));
        when(petitionRepository.updatePetitionStatus("1", "2")).thenReturn(Mono.just(updated));
        when(petitionValidatorUseCase.findById(existing.getLoanType().getId()))
                .thenReturn(Mono.just(existing.getLoanType())); // ✅ usar id del util
        when(petitionMapper.toSqsMessage(updated)).thenReturn(petitionMessage);
        when(petitionMapper.toReportSqsMessage(updated)).thenReturn(reportMessage);
        when(notificationMessageRepository.sendRequestStatusNotification(anyString()))
                .thenReturn(Mono.empty());
        when(reportRepository.addNewPetitionReport(anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(petitionMessagingUseCase.updatePetition(petition, token))
                .expectNextMatches(p -> p.getId().equals("1") && p.getStatus().getName().equals("APROBADA"))
                .verifyComplete();
    }

    @Test
    void mustFailWhenPetitionNotFoundInUpdatePetition() {
        String token = "Bearer token";
        when(authUseCase.validateAndGetUserRole(token, Role.ROLE_ASESOR.name())).thenReturn(Mono.just(new User()));
        when(petitionRepository.findById("1")).thenReturn(Mono.empty());

        Petition petition = new Petition();
        petition.setId("1");
        petition.setStatus(StatusUtil.status());

        StepVerifier.create(petitionMessagingUseCase.updatePetition(petition, token))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.PETITION_NOT_FOUND))
                .verify();
    }

    @Test
    void mustFailWhenStatusNotFoundInUpdatePetition() {
        String token = "Bearer token";
        Petition petition = new Petition();
        petition.setId("1");
        petition.setStatus(StatusUtil.status());
        petition.getStatus().setName("INVALID");

        Petition existing = new Petition();
        existing.setId("1");

        when(authUseCase.validateAndGetUserRole(token, Role.ROLE_ASESOR.name())).thenReturn(Mono.just(new User()));
        when(petitionRepository.findById("1")).thenReturn(Mono.just(existing));
        when(petitionValidatorUseCase.getStatusByName("INVALID")).thenReturn(Mono.empty());

        StepVerifier.create(petitionMessagingUseCase.updatePetition(petition, token))
                .expectErrorMatches(e -> e instanceof BusinessException &&
                        e.getMessage().equals(BusinessException.STATUS_NOT_FOUND))
                .verify();
    }

    @Test
    void mustEnqueueForDebtCapacitySuccessfully() {
        Petition petition = PetitionUtil.petition();
        User user = UserUtil.user();
        List<Petition> approved = List.of(PetitionUtil.petition());

        ValidationSqsMessage message = new ValidationSqsMessage();
        message.setPetitionId("1");
        message.setEmail(user.getEmail());
        message.setLoanTypeName(petition.getLoanType().getName());
        message.setAmount(petition.getAmount());
        message.setTerm(petition.getTerm());
        message.setBaseSalary(user.getBaseSalary());
        message.setInterestRate(petition.getLoanType().getInterestRate());
        message.setApprovedLoans(List.of()); // si quieres dejarlo vacío

        when(petitionMapper.toValidationSqsMessage(petition, user, approved)).thenReturn(message);
        when(debtCapacityRepository.calculateDebtCapacity(message)).thenReturn(Mono.just("OK"));

        StepVerifier.create(petitionMessagingUseCase.enqueueForDebtCapacity(petition, user, approved))
                .expectNext("OK")
                .verifyComplete();
    }

}
