package co.com.crediya.usecase.registerloanapplication;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationRegisterInvalidDataException;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loanapplicationstate.exceptions.LoanApplicationStateNotFoundException;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.exceptions.LoanTypeNotFoundException;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

@RequiredArgsConstructor
public class RegisterLoanApplicationUseCase {

    private static final String PENDING_REVIEW_STATE_CODE = "PENDING_REVIEW";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationStateRepository  loanApplicationStateRepository;

    public Mono<LoanApplication> registerLoanApplication(LoanApplication loanApplication) {
       return this.validateLoanApplication(loanApplication)
               .then(Mono.defer(() -> this.validateExistingLoanType(loanApplication.getLoanType().getId())))
               .then(Mono.defer(this::findPendingReviewLoanApplicationState))
               .map(state -> this.setLoanApplicationState(loanApplication, state))
               .flatMap(this.loanApplicationRepository::save);
    }

    private Mono<Void> validateLoanApplication(LoanApplication loanApplication) {
        return Mono.defer(() -> {
            if (Objects.isNull(loanApplication)) {
                return Mono.error(new LoanApplicationRegisterInvalidDataException("The loan application is required."));
            }

            return this.validateAmount(loanApplication.getAmount())
                    .then(this.validateTerm(loanApplication.getTerm()))
                    .then(this.validateEmail(loanApplication.getEmail()))
                    .then(this.validateLoanType(loanApplication.getLoanType()));
        });
    }

    private Mono<Void> validateExistingLoanType(Long loanTypeId) {
        return this.loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException("Loan type not found.")))
                .then();
    }

    private Mono<LoanApplicationState> findPendingReviewLoanApplicationState() {
        final var errorMessage = "Loan application state not found by the following code %s"
                .formatted(PENDING_REVIEW_STATE_CODE);

        return this.loanApplicationStateRepository.findByCode(PENDING_REVIEW_STATE_CODE)
                .switchIfEmpty(Mono.error(new LoanApplicationStateNotFoundException(errorMessage)));
    }

    private LoanApplication setLoanApplicationState(LoanApplication loanApplication, LoanApplicationState state) {
        loanApplication.setState(state);

        return loanApplication;
    }

    private Mono<Void> validateAmount(BigDecimal amount) {
        if (Objects.isNull(amount)) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The amount is required."));
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The amount must be greater than zero."));
        }

        return Mono.empty();
    }

    private Mono<Void> validateTerm(Integer term) {
        if (Objects.isNull(term)) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The term is required."));
        }

        if (term <= 0) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The term must be greater than zero."));
        }

        return Mono.empty();
    }

    private Mono<Void> validateEmail(String email) {
        final var emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        if (Objects.isNull(email)) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The email is required."));
        }

        if (!email.matches(emailRegex)) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The email is not valid."));
        }

        return Mono.empty();
    }

    private Mono<Void> validateLoanType(LoanType loanType) {
        if (Objects.isNull(loanType) || Objects.isNull(loanType.getId())) {
            return Mono.error(new LoanApplicationRegisterInvalidDataException("The loan type is required."));
        }

        return Mono.empty();
    }
}
