package co.com.crediya.api.handlers;

import co.com.crediya.api.dtos.ErrorResponseDto;
import co.com.crediya.api.exceptions.ValidationException;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationRegisterInvalidDataException;
import co.com.crediya.model.loantype.exceptions.LoanTypeNotFoundException;
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
                .onErrorResume(ValidationException.class, GlobalErrorHandler::handleValidationException)
                .onErrorResume(LoanApplicationRegisterInvalidDataException.class,
                        GlobalErrorHandler::handleLoanApplicationRegisterInvalidDataException)
                .onErrorResume(LoanTypeNotFoundException.class, GlobalErrorHandler::handleLoanTypeNotFoundException)
                .onErrorResume(Exception.class, GlobalErrorHandler::handleUnexpectedException);
    }

    private static Mono<ServerResponse> handleValidationException(ValidationException e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private static Mono<ServerResponse> handleLoanApplicationRegisterInvalidDataException(
            LoanApplicationRegisterInvalidDataException e) {

        final var errorResponse = new ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), e.getMessage());

        return ServerResponse.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

    private static Mono<ServerResponse> handleLoanTypeNotFoundException(LoanTypeNotFoundException e) {
        final var errorResponse = new ErrorResponseDto(HttpStatus.CONFLICT.value(), e.getMessage());

        return ServerResponse.status(HttpStatus.CONFLICT)
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
