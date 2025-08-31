package co.com.crediya.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreatePetitionDTO(
        @NotBlank(message = "Identification number is required")
        String identificationNumber,
        @Min(message = "Amount must be greater than zero", value = 1)
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount,
        @NotNull(message = "Term is required")
        @Min(message = "Term must be at least 1", value = 1)
        @Positive(message = "Term must be a positive number")
        Integer term,
        @NotBlank(message = "Loan type name is required")
        @Size(min = 2, max = 50, message = "Loan type name must be between 2 and 50 characters")
        String loanTypeName
) {}
