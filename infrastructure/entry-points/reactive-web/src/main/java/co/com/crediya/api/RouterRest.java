package co.com.crediya.api;

import co.com.crediya.api.config.RequestPath;
import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.dto.response.ErrorResponseDTO;
import co.com.crediya.api.dto.response.PetitionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final RequestPath requestPath;
    @Bean
    @RouterOperations(
            @RouterOperation(
                    path = "/api/v1/requests",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = org.springframework.web.bind.annotation.RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "savePetition",
                    operation = @Operation(
                            summary = "Create Request",
                            operationId = "savePetition",
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(implementation = CreatePetitionDTO.class))
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful operation",
                                            content = @Content(schema = @Schema(implementation = PetitionResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Invalid input",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                                    )
                            }
                    )
            )
    )
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(requestPath.getRequest()), handler::savePetition);
    }
}
