package co.com.crediya.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Loan application decision request.")
public record ApproveOrRejectLoanApplicationRequestDto(
        @NotNull(message = "The approval state is required.")
        @Schema(name = "approval_state",
                description = "Decision for the loan application. Must be either \"APPROVED\" or \"REJECTED\"",
                example = "APPROVED", requiredMode = Schema.RequiredMode.REQUIRED)
        ApprovalStateDto approvalState
) { }
