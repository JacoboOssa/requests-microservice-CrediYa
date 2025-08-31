package co.com.crediya.api.dto.response;

import java.math.BigDecimal;

public record LoanTypeResponseDTO(
        String id,
        String name,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Double interestRate,
        boolean automaticValidation
) {}
