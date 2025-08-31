package co.com.crediya.api.dto.response;

import lombok.Builder;

@Builder
public record StatusResponseDTO(
        String id,
        String name,
        String description
) {
}
