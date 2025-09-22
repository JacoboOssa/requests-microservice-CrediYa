package co.com.crediya.usecase.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PetitionSqsMessage {
    private String petitionId;
    private String email;
    private String statusName;
    private String statusDescription;
    private String loanTypeName;
    private BigDecimal amount;
    private int term;

    public String toJson() {
        return "{"
                + "\"petitionId\":\"" + petitionId + "\","
                + "\"email\":\"" + email + "\","
                + "\"statusName\":\"" + statusName + "\","
                + "\"statusDescription\":\"" + statusDescription + "\","
                + "\"loanTypeName\":\"" + loanTypeName + "\","
                + "\"amount\":" + amount + ","
                + "\"term\":" + term
                + "}";
    }
}



