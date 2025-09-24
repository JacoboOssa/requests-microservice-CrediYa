package co.com.crediya.usecase.util;

import co.com.crediya.model.status.Status;

public class StatusUtil {
    public static Status status(){
        return Status.builder()
                .id("1")
                .name("PENDIENTE")
                .description("Pending status")
                .build();
    }
}
