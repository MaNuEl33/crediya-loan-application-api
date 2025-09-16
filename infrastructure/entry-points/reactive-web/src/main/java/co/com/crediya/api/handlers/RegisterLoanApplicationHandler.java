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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

@Component
@RequiredArgsConstructor
@Log4j2
public class RegisterLoanApplicationHandler {

    private final RegisterLoanApplicationUseCase useCase;
    private final LoanApplicationRegisterDtoMapper dtoMapper;
    private final Validator validator;

    public Mono<ServerResponse> listenRegisterLoanApplication(ServerRequest serverRequest) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth ->
                    serverRequest.bodyToMono(LoanApplicationRegisterRequestDto.class)
                            .transform(requestBody -> ValidatorHelper.validateObject(requestBody, validator))
                            .handle((LoanApplicationRegisterRequestDto requestDto, SynchronousSink<LoanApplicationRegisterRequestDto> sink) -> this.validateEmail(requestDto, auth, sink))
                            .map(this.dtoMapper::toLoanApplicationModel)
                            .flatMap(this.useCase::registerLoanApplication)
                            .map(this.dtoMapper::toLoanApplicationRegisterResponseDto)
                            .flatMap(r -> ServerResponse.status(HttpStatus.CREATED)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(r))
                            .contextWrite(ctx -> ctx.put("token", auth.getCredentials()))
                            .doFirst(() -> log.info("New request to register a loan application."))
                            .doOnSuccess(r -> log.info("The loan application was successfully registered."))
                            .doOnError(err -> log.error("An error occurred while trying to register a loan application.", err))
                );
    }

    private void validateEmail(LoanApplicationRegisterRequestDto requestDto, Authentication auth, SynchronousSink<LoanApplicationRegisterRequestDto> sink) {
        final var errorMessage = "The email address used for authentication must be the same as the email address used in the loan application.";

        if (String.valueOf(auth.getPrincipal()).equals(requestDto.email())) {
            sink.next(requestDto);
        } else {
            sink.error(new EmailInvalidException(errorMessage));
        }
    }
}
