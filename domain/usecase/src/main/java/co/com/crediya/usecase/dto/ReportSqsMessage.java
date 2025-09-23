package co.com.crediya.usecase.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReportSqsMessage {
    private String petitionId;
    private String email;
    private BigDecimal amount;

    public String toJson() {
        return "{"
                + "\"petitionId\":\"" + petitionId + "\","
                + "\"email\":\"" + email + "\","
                + "\"amount\":" + amount
                + "}";
    }
}
