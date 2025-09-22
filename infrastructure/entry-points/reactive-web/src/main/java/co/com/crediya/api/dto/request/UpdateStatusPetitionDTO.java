package co.com.crediya.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusPetitionDTO(
        String id,
        @NotBlank(message = "Status is required")
        String status
) {
}
