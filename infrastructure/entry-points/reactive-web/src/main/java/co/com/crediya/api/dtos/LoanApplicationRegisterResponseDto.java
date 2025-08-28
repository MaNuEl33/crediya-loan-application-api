package co.com.crediya.api.dtos;

import java.math.BigDecimal;

public record LoanApplicationRegisterResponseDto(
        Long id,
        BigDecimal amount,
        Integer term
) {
}
