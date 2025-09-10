package co.com.crediya.api.handlers;

import co.com.crediya.api.dtos.ErrorResponseDto;
import co.com.crediya.api.exceptions.EmailInvalidException;
import co.com.crediya.api.exceptions.ValidationException;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationNotFoundException;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationRegisterInvalidDataException;
import co.com.crediya.model.loanapplicationstate.exceptions.ApproveOrRejectLoanApplicationInvalidDataException;
import co.com.crediya.model.loantype.exceptions.LoanTypeAlreadyProcessedException;
import co.com.crediya.model.loantype.exceptions.LoanTypeNotFoundException;
import co.com.crediya.model.manualvalidationapplicationreport.exceptions.ManualValidationApplicationReportInvalidDataException;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@UtilityClass
public class GlobalErrorHandler {

    public static HandlerFilterFunction<ServerResponse, ServerResponse> errorHandler() {
        return (request, next) ->next.handle(request)
                .onErrorResume(ValidationException.class, GlobalErrorHandler::handleInvalidDataException)
                .onErrorResume(LoanApplicationRegisterInvalidDataException.class,
                        GlobalErrorHandler::handleInvalidDataException)
                .onErrorResume(ManualValidationApplicationReportInvalidDataException.class,
                        GlobalErrorHandler::handleInvalidDataException)
                .onErrorResume(ApproveOrRejectLoanApplicationInvalidDataException.class,
                        GlobalErrorHandler::handleInvalidDataException)
                .onErrorResume(LoanApplicationNotFoundException.class,
                        GlobalErrorHandler::handleResourceNotFoundException)
                .onErrorResume(LoanTypeNotFoundException.class, GlobalErrorHandler::handleResourceConflictException)
                .onErrorResume(LoanTypeAlreadyProcessedException.class, GlobalErrorHandler::handleResourceConflictException)
                .onErrorResume(EmailInvalidException.class, GlobalErrorHandler::handleEmailInvalidException)
                .onErrorResume(Exception.class, GlobalErrorHandler::handleUnexpectedException);
    }

    private static Mono<ServerResponse> handleInvalidDataException(Exception e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private static Mono<ServerResponse> handleResourceConflictException(Exception e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.CONFLICT.value(), e.getMessage());

        return ServerResponse.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private static Mono<ServerResponse> handleEmailInvalidException(EmailInvalidException e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), e.getMessage());

        return ServerResponse.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private static Mono<ServerResponse> handleResourceNotFoundException(Exception e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.NOT_FOUND.value(), e.getMessage());

        return ServerResponse.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private static Mono<ServerResponse> handleUnexpectedException(Exception e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error has occurred. Please try again in a moment.");

        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }
}
