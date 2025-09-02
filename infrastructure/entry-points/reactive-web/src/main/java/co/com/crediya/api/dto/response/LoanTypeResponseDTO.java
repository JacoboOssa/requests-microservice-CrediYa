package co.com.crediya.api.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LoanTypeResponseDTO(
        String id,
        String name,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Double interestRate,
        boolean automaticValidation
) {}
