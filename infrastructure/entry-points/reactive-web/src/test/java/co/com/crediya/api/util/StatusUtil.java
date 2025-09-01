package co.com.crediya.api.util;

import co.com.crediya.api.dto.response.StatusResponseDTO;
import co.com.crediya.model.status.Status;

public class StatusUtil {
    public static Status status(){
        return Status.builder()
                .id("1")
                .name("PENDING")
                .description("Pending status")
                .build();
    }

    public static StatusResponseDTO statusResponseDTO(){
        return StatusResponseDTO.builder()
                .id("1")
                .name("PENDING")
                .description("Pending status")
                .build();
    }
}
