package co.com.crediya.api.dtos;

import jakarta.validation.constraints.NotNull;

public record ApproveOrRejectLoanApplicationRequestDto(
        @NotNull(message = "The approval state is required.")
        ApprovalStateDto approvalState
) { }
