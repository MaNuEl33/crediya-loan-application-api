package co.com.crediya.api.dtos;

import java.math.BigDecimal;

public record ManualValidationApplicationReportResponseElementDto(
        Long applicationId,
        BigDecimal amount,
        Integer term,
        String email,
        String loanType,
        BigDecimal interestRate,
        String applicationState
) {
}
