package co.com.crediya.usecase.petition.util;

import co.com.crediya.model.status.Status;

public class StatusUtil {
    public static Status status(){
        return Status.builder()
                .id("1")
                .name("PENDING")
                .description("Pending status")
                .build();
    }
}
