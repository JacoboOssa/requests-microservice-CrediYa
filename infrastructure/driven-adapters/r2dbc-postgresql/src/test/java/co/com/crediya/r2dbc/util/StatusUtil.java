package co.com.crediya.r2dbc.util;

import co.com.crediya.model.status.Status;

public class StatusUtil {
    public static Status status(){
        return Status.builder()
                .id("1")
                .name("PENDING")
                .description("Pending status")
                .build();
    }

    public static Status status2(){
        return Status.builder()
                .id("2")
                .name("APROBADA")
                .description("Approved status")
                .build();
    }
}
