package co.com.crediya.api.handlers;

import co.com.crediya.api.dtos.ApproveOrRejectLoanApplicationRequestDto;
import co.com.crediya.api.helpers.ValidatorHelper;
import co.com.crediya.api.mappers.ApproveOrRejectLoanApplicationDtoMapper;
import co.com.crediya.usecase.approveorrejectloanapplication.ApproveOrRejectLoanApplicationUseCase;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class ApproveOrRejectLoanApplicationHandler {

    private final ApproveOrRejectLoanApplicationUseCase useCase;
    private final ApproveOrRejectLoanApplicationDtoMapper dtoMapper;
    private final Validator validator;

    public Mono<ServerResponse> listenApproveOrRejectLoanApplication(ServerRequest serverRequest) {
        final var id = Long.valueOf(serverRequest.pathVariable("id"));

        return serverRequest.bodyToMono(ApproveOrRejectLoanApplicationRequestDto.class)
                .transform(requestBody -> ValidatorHelper.validateObject(requestBody, validator))
                .map(requestBody -> this.dtoMapper.toLoanApplicationApprovalState(requestBody.approvalState()))
                .flatMap(approvalState -> this.useCase.approveOrRejectLoanApplication(id, approvalState))
                .then(Mono.defer(() -> ServerResponse.noContent().build()))
                .doFirst(() -> log.info("New request to approve or reject a loan application with id {}", id))
                .doOnSuccess(r -> log.info("The loan application was approved or rejected successfully."))
                .doOnError(err -> log.error("Error approving or rejecting the loan application.", err));
    }
}
