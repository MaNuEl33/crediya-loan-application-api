package co.com.crediya.usecase.registerloanapplication;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationRegisterInvalidDataException;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loanapplicationstate.exceptions.LoanApplicationStateNotFoundException;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
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
        this.validateLoanApplication(loanApplication);

       return this.validateExistingLoanType(loanApplication.getLoanType().getId())
               .then(this.findPendingReviewLoanApplicationState())
               .map(state -> this.setLoanApplicationState(loanApplication, state))
               .flatMap(this.loanApplicationRepository::save);
    }

    private void validateLoanApplication(LoanApplication loanApplication) {
        if (Objects.isNull(loanApplication)) {
            throw new LoanApplicationRegisterInvalidDataException("The loan application is required.");
        }

        if (Objects.isNull(loanApplication.getAmount())) {
            throw new LoanApplicationRegisterInvalidDataException("The amount is required.");
        }

        if (loanApplication.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LoanApplicationRegisterInvalidDataException("The amount must be greater than zero.");
        }

        if (Objects.isNull(loanApplication.getTerm())) {
            throw new LoanApplicationRegisterInvalidDataException("The term is required.");
        }

        if (loanApplication.getTerm() <= 0) {
            throw new LoanApplicationRegisterInvalidDataException("The term must be greater than zero.");
        }

        if (Objects.isNull(loanApplication.getEmail())) {
            throw new LoanApplicationRegisterInvalidDataException("The email is required.");
        }

        if (this.isNotValidEmail(loanApplication.getEmail())) {
            throw new LoanApplicationRegisterInvalidDataException("The email is not valid.");
        }

        if (Objects.isNull(loanApplication.getLoanType()) || Objects.isNull(loanApplication.getLoanType().getId())) {
            throw new LoanApplicationRegisterInvalidDataException("The loan type is required.");
        }
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

    private boolean isNotValidEmail(String email) {
        final var emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

        return !email.matches(emailRegex);
    }
}
