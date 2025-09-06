package co.com.crediya.api.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ManualValidationApplicationReportRequestDto(
        Integer term,
        String email,
        Long loanTypeId,
        Long applicationStateId,
        @NotNull
        @Min(value = 1, message = "The page number must be greater than 0.")
        Integer pageNumber,
        @NotNull
        @Min(value = 1, message = "The page size must be greater than 0.")
        Integer pageSize
) {
}
