package co.com.crediya.api.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PetitionResponseDTO(
//        String id,
        int term,
        BigDecimal amount,
        String email,
        StatusResponseDTO status,
        LoanTypeResponseDTO loanType
) {

}
