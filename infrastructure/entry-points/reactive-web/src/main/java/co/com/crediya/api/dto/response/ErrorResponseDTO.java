package co.com.crediya.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error Response DTO")
public record ErrorResponseDTO(
        @Schema(description = "Error message")
        String message,

        @Schema(description = "HTTP method used in the request")
        String method,

        @Schema(description = "Request path that caused the error")
        String path,

        @Schema(description = "Error type")
        String error,

        @Schema(description = "HTTP status code")
        int status
) {}

