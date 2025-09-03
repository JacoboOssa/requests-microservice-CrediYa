package co.com.crediya.api;

import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.mapper.PetitionDTOMapper;
import co.com.crediya.api.validator.PetitionValidator;
import co.com.crediya.model.exception.JwtException;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.usecase.petition.PetitionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {
    private final PetitionUseCase petitionUseCase;
    private final PetitionValidator petitionValidator;
    private final PetitionDTOMapper petitionDTOMapper;

    public Mono<ServerResponse> savePetition(ServerRequest serverRequest) {

        String token = serverRequest.headers().firstHeader("Authorization");


        return serverRequest.bodyToMono(CreatePetitionDTO.class)
                .flatMap(petitionValidator::validate)
                .flatMap(dto -> {
                    Petition petition = petitionDTOMapper.toPetition(dto);
                    return petitionUseCase.registerPetition(petition, dto.identificationNumber(), token);
                })
                .doOnNext(savedPetition -> log.info("Petition saved successfully: {}", savedPetition.getId()))
                .map(petitionDTOMapper::toPetitionResponseDTO)
                .flatMap(responseDTO ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(responseDTO))
                .doOnError(error -> log.error("Error processing petition: {}", error.getMessage()));
    }


}
