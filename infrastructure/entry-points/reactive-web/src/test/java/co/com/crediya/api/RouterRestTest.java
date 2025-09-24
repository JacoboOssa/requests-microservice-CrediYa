package co.com.crediya.api;

import co.com.crediya.api.config.RequestPath;
import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.dto.response.PetitionResponseDTO;
import co.com.crediya.api.exceptionhandler.GlobalErrorAttributes;
import co.com.crediya.api.exceptionhandler.GlobalExceptionHandler;
import co.com.crediya.api.mapper.PetitionDTOMapper;
import co.com.crediya.api.util.PetitionUtil;
import co.com.crediya.api.validator.PetitionValidator;
import co.com.crediya.model.exception.AuthorizationException;
import co.com.crediya.model.exception.BusinessException;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.usecase.petition.PetitionUseCase;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@EnableConfigurationProperties(RequestPath.class)
@TestPropertySource(properties = {"routes.paths.request=/api/v1/requests","routes.paths.get-all-requests=/api/v1/requests", "routes.paths.update-request-status=/api/v1/requests/{id}/status"})
@WebFluxTest
@Import({GlobalErrorAttributes.class, GlobalExceptionHandler.class})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PetitionDTOMapper petitionDTOMapper;

    @MockitoBean
    private PetitionValidator petitionValidator;

    @MockitoBean
    private PetitionUseCase petitionUseCase;

    @MockitoBean
    private PetitionMessagingUseCase petitionMessagingUseCase;


    @Autowired
    private RequestPath requestPath;

    private String registerRequestPath = "/api/v1/requests";

    @Test
    void shouldLoadTaskPathProperties() {
        assertEquals(registerRequestPath, requestPath.getRequest());
    }

    @Test
    void mustRegisterRequest() {
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.just(PetitionUtil.createPetitionDTO()));
        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class))).thenReturn(Mono.just(PetitionUtil.petition()));
        when(petitionDTOMapper.toPetitionResponseDTO(any(Petition.class))).thenReturn(PetitionUtil.petitionResponseDTO());

        webTestClient.post()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer validToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetitionResponseDTO.class)
                .value(response -> {
                    Assertions.assertThat(response).isNotNull();
                    Assertions.assertThat(response.loanType().name()).isEqualTo(PetitionUtil.petitionResponseDTO().loanType().name());
                }
        );
    }

    @Test
    void mustFailWhenLoanTypeNotExist() {
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.just(PetitionUtil.createPetitionDTO()));
        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessException.LOAN_TYPE_NOT_FOUND)));

        webTestClient.post()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer validToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo(BusinessException.LOAN_TYPE_NOT_FOUND)
                .jsonPath("$.method").isEqualTo("POST")
                .jsonPath("$.path").isEqualTo(registerRequestPath);
    }

    @Test
    void mustFailWhenAmountIsOutOfRange() {
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.just(PetitionUtil.createPetitionDTO()));
        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessException.AMOUNT_OUT_OF_RANGE)));

        webTestClient.post()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer validToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo(BusinessException.AMOUNT_OUT_OF_RANGE)
                .jsonPath("$.method").isEqualTo("POST")
                .jsonPath("$.path").isEqualTo(registerRequestPath);
    }

    @Test
    void mustFailWhenTokenIsInvalid(){
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.just(PetitionUtil.createPetitionDTO()));
        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new JwtException(JwtException.INVALID_TOKEN)));

        webTestClient.post()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer invalidToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo(JwtException.INVALID_TOKEN)
                .jsonPath("$.method").isEqualTo("POST")
                .jsonPath("$.path").isEqualTo(registerRequestPath);
    }

    @Test
    void mustFailWhenRolHasNoPermission(){
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.just(PetitionUtil.createPetitionDTO()));
        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class)))
                .thenReturn(Mono.error(new AuthorizationException(AuthorizationException.FORBIDDEN)));

        webTestClient.post()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer noPermissionToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo(AuthorizationException.FORBIDDEN)
                .jsonPath("$.method").isEqualTo("POST")
                .jsonPath("$.path").isEqualTo(registerRequestPath);
    }

    @Test
    void mustFailSavePetitionWhenConstraintViolationAppears(){
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.error(new ConstraintViolationException(Set.of())));

        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class))).thenReturn(Mono.just(PetitionUtil.petition()));
        when(petitionDTOMapper.toPetitionResponseDTO(any(Petition.class))).thenReturn(PetitionUtil.petitionResponseDTO());

        webTestClient.post()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer validToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Validation Error")
                .jsonPath("$.status").isEqualTo(400);

    }

    @Test
    void mustRetrieveAllNoApprovedPetitions(){
        when(petitionUseCase.getAllPetitionsPaginable(any(), any(Integer.class),any(Integer.class),any(String.class)))
                .thenReturn(Flux.just(PetitionUtil.listPetitionsDTO()));

        webTestClient.get()
                .uri(registerRequestPath)
                .header("Authorization", "Bearer validToken")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].email").isEqualTo(PetitionUtil.listPetitionsDTO().getEmail())
                .jsonPath("$[0].term").isEqualTo(PetitionUtil.listPetitionsDTO().getTerm());
    }

    @Test
    void mustUpdatePetitionStatusSuccessfully() {
        String petitionId = "123";
        String token = "Bearer validToken";
        var updateStatusDTO = PetitionUtil.updateStatusPetitionDTO();
        var petition = PetitionUtil.petition();
        var responseDTO = PetitionUtil.petitionResponseDTO();

        when(petitionValidator.validate(any())).thenReturn(Mono.just(updateStatusDTO));
        when(petitionDTOMapper.toStatus(any())).thenReturn(petition.getStatus());
        when(petitionMessagingUseCase.updatePetition(any(Petition.class), any(String.class)))
                .thenReturn(Mono.just(petition));
        when(petitionDTOMapper.toPetitionResponseDTO(any(Petition.class)))
                .thenReturn(responseDTO);

        webTestClient.put()
                .uri("/api/v1/requests/{id}/status", petitionId)
                .header("Authorization", token)
                .bodyValue(updateStatusDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PetitionResponseDTO.class)
                .value(resp -> {
                    Assertions.assertThat(resp).isNotNull();
                    Assertions.assertThat(resp.status().id()).isEqualTo(petition.getStatus().getId());
                });
    }

    @Test
    void mustFailUpdatePetitionWhenTokenInvalid() {
        String petitionId = "123";
        var updateStatusDTO = PetitionUtil.updateStatusPetitionDTO();

        when(petitionValidator.validate(any())).thenReturn(Mono.just(updateStatusDTO));
        when(petitionDTOMapper.toStatus(any())).thenReturn(PetitionUtil.petition().getStatus());
        when(petitionMessagingUseCase.updatePetition(any(Petition.class), any(String.class)))
                .thenReturn(Mono.error(new JwtException(JwtException.INVALID_TOKEN)));

        webTestClient.put()
                .uri("/api/v1/requests/{id}/status", petitionId)
                .header("Authorization", "Bearer invalidToken")
                .bodyValue(updateStatusDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo(JwtException.INVALID_TOKEN)
                .jsonPath("$.method").isEqualTo("PUT")
                .jsonPath("$.path").isEqualTo("/api/v1/requests/" + petitionId + "/status");
    }

    @Test
    void mustFailUpdatePetitionWhenStatusNotAllowed() {
        String petitionId = "123";
        var updateStatusDTO = PetitionUtil.updateStatusPetitionDTO();

        when(petitionValidator.validate(any())).thenReturn(Mono.just(updateStatusDTO));
        when(petitionDTOMapper.toStatus(any())).thenReturn(PetitionUtil.petition().getStatus());
        when(petitionMessagingUseCase.updatePetition(any(Petition.class), any(String.class)))
                .thenReturn(Mono.error(new BusinessException(BusinessException.STATUS_NOT_FOUND)));

        webTestClient.put()
                .uri("/api/v1/requests/{id}/status", petitionId)
                .header("Authorization", "Bearer validToken")
                .bodyValue(updateStatusDTO)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.message").isEqualTo(BusinessException.STATUS_NOT_FOUND)
                .jsonPath("$.method").isEqualTo("PUT")
                .jsonPath("$.path").isEqualTo("/api/v1/requests/" + petitionId + "/status");
    }






}
