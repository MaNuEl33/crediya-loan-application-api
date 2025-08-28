package co.com.crediya.api.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanApplicationRegisterRequestDto(
        @NotNull(message = "The amount is required.")
        @DecimalMin(value = "0", message = "The amount must be greater than zero.")
        BigDecimal amount,
        @NotNull(message = "The term is required.")
        @Min(value = 0, message = "The term must be greater than zero.")
        Integer term,
        @NotBlank(message = "The email is required.")
        @Email(message = "The email is not valid.")
        String email,
        @NotNull(message = "The loan type is required.")
        Long loanTypeId
) {
}
