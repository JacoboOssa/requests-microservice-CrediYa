package co.com.crediya.api;

import co.com.crediya.api.config.RequestPath;
import co.com.crediya.api.dto.request.CreatePetitionDTO;
import co.com.crediya.api.dto.response.ErrorResponseDTO;
import co.com.crediya.api.dto.response.PetitionResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final RequestPath requestPath;
    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/requests",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.POST,
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
            ),
            @RouterOperation(
                    path = "/api/v1/requests",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getPetitions",
                    operation = @Operation(
                            summary = "Get Requests",
                            operationId = "getPetitions",
                            parameters = {
                                    @Parameter(
                                            name = "status",
                                            description = "Filter by status (comma-separated values)",
                                            required = false,
                                            example = "PENDIENTE,RECHAZADA"
                                    ),
                                    @Parameter(
                                            name = "page",
                                            description = "Page number for pagination",
                                            required = false,
                                            example = "0"
                                    ),
                                    @Parameter(
                                            name = "size",
                                            description = "Number of items per page",
                                            required = false,
                                            example = "20"
                                    )

                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful operation",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PetitionResponseDTO.class))
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Unauthorized",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Forbidden",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Internal server error",
                                            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/requests/{id}/status",
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "updatePetitionStatus",
                    operation = @Operation(
                            summary = "Update Request Status",
                            operationId = "updatePetitionStatus",
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            description = "ID of the request to update",
                                            required = true,
                                            example = "123e4567-e89b-12d3-a456-426614174000"
                                    )
                            },
                            requestBody = @RequestBody(
                                    content = @Content(schema = @Schema(implementation = String.class)),
                                    description = "New status value (e.g., 'APROBADA', 'RECHAZADA')",
                                    required = true
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
                                            responseCode = "404",
                                            description = "Request not found",
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
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(requestPath.getRequest()), handler::savePetition)
                .andRoute(GET(requestPath.getGetAllRequests()), handler::getPetitions)
                .andRoute(PUT(requestPath.getUpdateRequestStatus()), handler::updatePetitionStatus);
    }
}
