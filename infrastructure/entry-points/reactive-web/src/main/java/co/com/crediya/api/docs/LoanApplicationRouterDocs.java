package co.com.crediya.api.docs;

import co.com.crediya.api.dtos.ApproveOrRejectLoanApplicationRequestDto;
import co.com.crediya.api.dtos.ErrorResponseDto;
import co.com.crediya.api.handlers.ApproveOrRejectLoanApplicationHandler;
import co.com.crediya.api.handlers.ManualValidationApplicationReportHandler;
import co.com.crediya.api.handlers.RegisterLoanApplicationHandler;
import co.com.crediya.api.routers.LoanApplicationRouterRest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Tag(name = "loan-applications")
public class LoanApplicationRouterDocs {

    @Bean
    @RouterOperations(
        @RouterOperation(
                path = "/solicitudes/{id}/decision",
                operation = @Operation(
                        summary = "Approve or reject loan application",
                        description = "Approve or reject a loan application that is pending validation.",
                        tags = "loan-applications",
                        operationId = "approve-or-reject-loan-application",
                        parameters = {
                                @Parameter(
                                        name = "id",
                                        description = "A positive integer representing the loan application ID.",
                                        in = ParameterIn.PATH,
                                        required = true,
                                        schema = @Schema(
                                                type = "integer",
                                                minimum = "1",
                                                example = "1"
                                        )
                                )
                        },
                        requestBody = @RequestBody(
                                required = true,
                                content = @Content(
                                        schema = @Schema(implementation = ApproveOrRejectLoanApplicationRequestDto.class),
                                        examples = {
                                                @ExampleObject(
                                                        name = "Valid request body",
                                                        value = """
                                                                {
                                                                    "approval_state": "APPROVED"
                                                                }
                                                                """
                                                )
                                        }
                                )
                        ),
                        responses = {
                                @ApiResponse(
                                        responseCode = "204",
                                        description = "Loan application state updated successfully."
                                ),
                                @ApiResponse(
                                        responseCode = "400",
                                        description = "Loan application decision bad request",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponseDto.class),
                                                examples = {
                                                        @ExampleObject(
                                                                name = "Invalid Loan Application Decision Data",
                                                                value = """
                                                                        {
                                                                          "status": 400,
                                                                          "message": "The approval state must be either \\"APPROVED\\" or \\"REJECTED\\"."
                                                                        }
                                                                        """
                                                        )
                                                }
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "401",
                                        description = "Unauthorized",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponseDto.class),
                                                examples = {
                                                        @ExampleObject(
                                                                name = "Invalid Credentials",
                                                                value = """
                                                                        {
                                                                           "status": 401,
                                                                           "message": "Invalid or missing authentication token."
                                                                         }
                                                                        """
                                                        )
                                                }
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "403",
                                        description = "Forbidden",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponseDto.class),
                                                examples = {
                                                        @ExampleObject(
                                                                name = "Insufficient Permissions",
                                                                value = """
                                                                        {
                                                                            "status": 403,
                                                                            "message": "You do not have permission to perform this action."
                                                                        }
                                                                        """
                                                        )
                                                }
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "404",
                                        description = "Not Found",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponseDto.class),
                                                examples = {
                                                        @ExampleObject(
                                                                name = "Loan Application Not Found",
                                                                value = """
                                                                        {
                                                                            "status": 404,
                                                                            "message": "Loan application not found."
                                                                        }
                                                                        """
                                                        )
                                                }
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "409",
                                        description = "Conflict",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponseDto.class),
                                                examples = {
                                                        @ExampleObject(
                                                                name = "Loan Application State Already Approved or Rejected",
                                                                value = """
                                                                        {
                                                                            "status": 409,
                                                                            "message": "Loan application has already been approved or rejected."
                                                                        }
                                                                        """
                                                        )
                                                }
                                        )
                                ),
                                @ApiResponse(
                                        responseCode = "500",
                                        description = "Internal Server Error",
                                        content = @Content(
                                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                schema = @Schema(implementation = ErrorResponseDto.class),
                                                examples = {
                                                        @ExampleObject(
                                                                name = "Unexpected Error",
                                                                value = """
                                                                        {
                                                                            "status": 500,
                                                                            "message": "Unexpected error occurred."
                                                                        }
                                                                        """
                                                        )
                                                }
                                        )
                                )
                        }
                )
        )
    )
    public RouterFunction<ServerResponse> approveOrRejectLoanApplicationDoc(
            LoanApplicationRouterRest router,
            RegisterLoanApplicationHandler registerHandler,
            ManualValidationApplicationReportHandler manualHandler,
            ApproveOrRejectLoanApplicationHandler approveRejectHandler) {
        return router.loanApplicationRoutes();
    }
}
