package co.com.crediya.usecase.approveorrejectloanapplication;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationNotFoundException;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import co.com.crediya.model.loanapplicationstate.exceptions.ApproveOrRejectLoanApplicationInvalidDataException;
import co.com.crediya.model.loanapplicationstate.exceptions.LoanApplicationStateNotFoundException;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.model.loantype.exceptions.LoanTypeAlreadyProcessedException;
import co.com.crediya.usecase.UseCaseTransactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.Objects;

@RequiredArgsConstructor
public class ApproveOrRejectLoanApplicationUseCase {

    private static final String APPROVED_STATE_CODE = "ACCEPTED";
    private static final String REJECTED_STATE_CODE = "REJECTED";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStateRepository loanApplicationStateRepository;
    private final LoanApplicationNotifier loanApplicationNotifier;

    @UseCaseTransactional
    public Mono<Void> approveOrRejectLoanApplication(Long loanApplicationId, LoanApplicationApprovalState approvalState) {
        return Mono.defer(() -> this.validateLoanApplicationId(loanApplicationId))
                .then(Mono.defer(() -> this.validateLoanApplicationApprovalState(approvalState)))
                .then(Mono.defer(() -> this.findLoanApplicationById(loanApplicationId)))
                .handle(this::validateLoanApplicationState)
                .flatMap(l -> this.setFinalState(l, approvalState))
                .flatMap(this.loanApplicationRepository::save)
                .flatMap(l -> Mono.when(
                        this.loanApplicationNotifier.notifyProcessedLoanApplication(l, approvalState),
                        this.notifyAcceptedLoanApplication(l, approvalState)));
    }

    private Mono<Void> validateLoanApplicationId(Long loanApplicationId) {
        if (Objects.isNull(loanApplicationId)) {
            return Mono.error(new ApproveOrRejectLoanApplicationInvalidDataException("The loan application id is required."));
        }

        return Mono.empty();
    }

    private Mono<Void> validateLoanApplicationApprovalState(LoanApplicationApprovalState approvalState) {
        if (Objects.isNull(approvalState)) {
            return Mono.error(new ApproveOrRejectLoanApplicationInvalidDataException("The approval state is required."));
        }

        return Mono.empty();
    }

    private Mono<LoanApplication> findLoanApplicationById(Long loanApplicationId) {
        final var notFoundErrorMessage = "The loan application with id %d does not exist.".formatted(loanApplicationId);

        return this.loanApplicationRepository.findById(loanApplicationId)
                .switchIfEmpty(Mono.error(new LoanApplicationNotFoundException(notFoundErrorMessage)))
                .flatMap(this::setLoanApplicationState);
    }

    private Mono<LoanApplication> setLoanApplicationState(LoanApplication loanApplication) {
        final var notFoundErrorMessage = "The loan application state with id %d does not exist."
                .formatted(loanApplication.getState().getId());

        return this.loanApplicationStateRepository.findById(loanApplication.getState().getId())
                .switchIfEmpty(Mono.error(new LoanApplicationStateNotFoundException(notFoundErrorMessage)))
                .map(state -> loanApplication.toBuilder().state(state).build());
    }

    private void validateLoanApplicationState(LoanApplication loanApplication, SynchronousSink<LoanApplication> sink) {
        if (APPROVED_STATE_CODE.equals(loanApplication.getState().getCode())) {
            sink.error(new LoanTypeAlreadyProcessedException("The loan application has already been approved."));
        } else if (REJECTED_STATE_CODE.equals(loanApplication.getState().getCode())) {
            sink.error(new LoanTypeAlreadyProcessedException("The loan application has already been rejected."));
        } else {
            sink.next(loanApplication);
        }
    }

    private Mono<LoanApplication> setFinalState(LoanApplication loanApplication, LoanApplicationApprovalState approvalState) {
        return switch (approvalState) {
            case APPROVED -> this.setApprovedFinalState(loanApplication);
            case REJECTED -> this.setRejectedFinalState(loanApplication);
        };
    }

    private Mono<LoanApplication> setApprovedFinalState(LoanApplication loanApplication) {
        final var notFoundErrorMessage = "The loan application state with code %s does not exist.".formatted(APPROVED_STATE_CODE);

        return this.loanApplicationStateRepository.findByCode(APPROVED_STATE_CODE)
                .switchIfEmpty(Mono.error(new LoanApplicationStateNotFoundException(notFoundErrorMessage)))
                .map(state -> loanApplication.toBuilder().state(state).build());
    }

    private Mono<LoanApplication> setRejectedFinalState(LoanApplication loanApplication) {
        final var notFoundErrorMessage = "The loan application state with code %s does not exist.".formatted(REJECTED_STATE_CODE);

        return this.loanApplicationStateRepository.findByCode(REJECTED_STATE_CODE)
                .switchIfEmpty(Mono.error(new LoanApplicationStateNotFoundException(notFoundErrorMessage)))
                .map(state -> loanApplication.toBuilder().state(state).build());
    }

    private Mono<Void> notifyAcceptedLoanApplication(LoanApplication loanApplication, LoanApplicationApprovalState approvalState) {
        return switch (approvalState) {
            case APPROVED -> this.loanApplicationNotifier.notifyAcceptedLoanApplication(loanApplication);
            case REJECTED -> Mono.empty();
        };
    }
}
