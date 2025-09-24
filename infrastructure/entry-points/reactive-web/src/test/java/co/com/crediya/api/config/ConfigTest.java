package co.com.crediya.api.config;

import co.com.crediya.api.Handler;
import co.com.crediya.api.RouterRest;
import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.mapper.PetitionDTOMapper;
import co.com.crediya.api.util.PetitionUtil;
import co.com.crediya.api.validator.PetitionValidator;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.usecase.petition.PetitionUseCase;
import co.com.crediya.usecase.petitionmessaging.PetitionMessagingUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = {"routes.paths.request=/api/v1/requests","routes.paths.get-all-requests=/api/v1/requests", "routes.paths.update-request-status=/api/v1/requests/{id}/status"})
@EnableConfigurationProperties(RequestPath.class)
@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

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

    @BeforeEach
    void setUp() {
        when(petitionUseCase.registerPetition(any(Petition.class), any(String.class), any(String.class))).thenReturn(Mono.just(PetitionUtil.petition()));
        when(petitionValidator.validate(any(CreatePetitionDTO.class))).thenReturn(Mono.just(PetitionUtil.createPetitionDTO()));
        when(petitionDTOMapper.toPetition(any(CreatePetitionDTO.class))).thenReturn(PetitionUtil.petition());
        when(petitionDTOMapper.toPetitionResponseDTO(any(Petition.class))).thenReturn(PetitionUtil.petitionResponseDTO());
    }


    @Test
    void corsConfigurationShouldAllowOrigins() {
        String origin = "/api/v1/requests";
        webTestClient.post()
                .uri(origin)
                .header("Authorization", "Bearer validToken")
                .bodyValue(PetitionUtil.createPetitionDTO())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}