package co.com.crediya.api.handlers;

import co.com.crediya.api.dtos.LoanApplicationRegisterRequestDto;
import co.com.crediya.api.exceptions.EmailInvalidException;
import co.com.crediya.api.helpers.ValidatorHelper;
import co.com.crediya.api.mappers.LoanApplicationRegisterDtoMapper;
import co.com.crediya.usecase.registerloanapplication.RegisterLoanApplicationUseCase;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class LoanApplicationHandler {

    private final RegisterLoanApplicationUseCase registerLoanApplicationUseCase;
    private final LoanApplicationRegisterDtoMapper loanApplicationRegisterDtoMapper;
    private final Validator validator;

    public Mono<ServerResponse> listenRegisterLoanApplication(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanApplicationRegisterRequestDto.class)
                .transform(requestBody -> ValidatorHelper.validateObject(requestBody, validator))
                .flatMap(requestDto -> this.validateEmail(requestDto).thenReturn(requestDto))
                .map(this.loanApplicationRegisterDtoMapper::toLoanApplicationModel)
                .flatMap(this.registerLoanApplicationUseCase::registerLoanApplication)
                .map(this.loanApplicationRegisterDtoMapper::toLoanApplicationRegisterResponseDto)
                .flatMap(r -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(r))
                .doFirst(() -> log.info("New request to register a loan application."))
                .doOnSuccess(r -> log.info("The loan application was successfully registered."))
                .doOnError(err -> log.error("An error occurred while trying to register a loan application.", err));
    }

    private Mono<Void> validateEmail(LoanApplicationRegisterRequestDto requestDto) {
        final var errorMessage = "The email address used for authentication must be the same as the email address used in the loan application.";

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth -> String.valueOf(auth.getPrincipal()).equals(requestDto.email()))
                .switchIfEmpty(Mono.error(new EmailInvalidException(errorMessage)))
                .then();
    }
}
