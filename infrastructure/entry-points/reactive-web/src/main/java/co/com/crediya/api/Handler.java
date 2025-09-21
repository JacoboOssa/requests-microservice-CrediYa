package co.com.crediya.api;

import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.mapper.PetitionDTOMapper;
import co.com.crediya.api.validator.PetitionValidator;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.usecase.petition.PetitionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

    public static final String AUTH_HEADER = "Authorization";
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_STATUS = "";


    private final PetitionUseCase petitionUseCase;
    private final PetitionValidator petitionValidator;
    private final PetitionDTOMapper petitionDTOMapper;

    public Mono<ServerResponse> savePetition(ServerRequest serverRequest) {

        String token = serverRequest.headers().firstHeader(AUTH_HEADER);

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

    public Mono<ServerResponse> getPetitions(ServerRequest request) {

        String statusParam = request.queryParam("status").orElse(DEFAULT_STATUS);
        List<String> requestedStatuses = Arrays.stream(statusParam.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        int page = Integer.parseInt(request.queryParam("page")
                .orElse(String.valueOf(DEFAULT_PAGE)));

        int size = Integer.parseInt(request.queryParam("size")
                .orElse(String.valueOf(DEFAULT_SIZE)));

        String token = request.headers().firstHeader(AUTH_HEADER);

        return petitionUseCase.getAllPetitionsPaginable(requestedStatuses, page, size, token)
                .collectList()
                .doOnSuccess(list -> log.info("Retrieved {} petitions", list.size()))
                .doOnError(error -> log.error("Error processing petitions: {}", error.getMessage()))
                .flatMap(petitions -> ServerResponse.ok().bodyValue(petitions));
    }



}
