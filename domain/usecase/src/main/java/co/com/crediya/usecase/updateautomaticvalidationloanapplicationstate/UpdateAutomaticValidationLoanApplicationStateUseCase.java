package co.com.crediya.usecase.updateautomaticvalidationloanapplicationstate;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationNotFoundException;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationCodeState;
import co.com.crediya.model.loanapplicationstate.exceptions.LoanApplicationStateNotFoundException;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.model.loantype.exceptions.LoanTypeNotFoundException;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.usecase.UseCaseTransactional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateAutomaticValidationLoanApplicationStateUseCase {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanApplicationStateRepository loanApplicationStateRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationNotifier loanApplicationNotifier;

    @UseCaseTransactional
    public Mono<Void> execute(Long loanApplicationId, LoanApplicationCodeState codeState) {
        return this.findLoanApplicationById(loanApplicationId)
                .flatMap(l -> this.setLoanApplicationState(l, codeState))
                .flatMap(this.loanApplicationRepository::save)
                .flatMap(this::setLoanType)
                .flatMap(l -> this.loanApplicationNotifier.notifyAutomaticValidationLoanApplicationState(l, codeState));
    }

    private Mono<LoanApplication> findLoanApplicationById(Long loanApplicationId) {
        final var notFoundErrorMessage = "The loan application with id %d does not exist.".formatted(loanApplicationId);

        return this.loanApplicationRepository.findById(loanApplicationId)
                .switchIfEmpty(Mono.error(new LoanApplicationNotFoundException(notFoundErrorMessage)));
    }

    private Mono<LoanApplication> setLoanApplicationState(LoanApplication loanApplication, LoanApplicationCodeState codeState) {
        final var notFoundErrorMessage = "The loan application state with code %s does not exist."
                .formatted(codeState.name());

        return this.loanApplicationStateRepository.findByCode(codeState.name())
                .switchIfEmpty(Mono.error(new LoanApplicationStateNotFoundException(notFoundErrorMessage)))
                .map(state -> loanApplication.toBuilder().state(state).build());
    }

    private Mono<LoanApplication> setLoanType(LoanApplication loanApplication) {
        final var notFoundErrorMessage = "The loan type with id %d does not exist."
                .formatted(loanApplication.getLoanType().getId());

        return this.loanTypeRepository.findById(loanApplication.getLoanType().getId())
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(notFoundErrorMessage)))
                .map(loanType -> loanApplication.toBuilder().loanType(loanType).build());
    }
}
