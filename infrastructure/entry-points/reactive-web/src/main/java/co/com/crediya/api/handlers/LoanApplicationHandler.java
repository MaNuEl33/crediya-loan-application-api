package co.com.crediya.api.handlers;

import co.com.crediya.api.dtos.LoanApplicationRegisterRequestDto;
import co.com.crediya.api.helpers.ValidatorHelper;
import co.com.crediya.api.mappers.LoanApplicationRegisterDtoMapper;
import co.com.crediya.usecase.registerloanapplication.RegisterLoanApplicationUseCase;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
}
