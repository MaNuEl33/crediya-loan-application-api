package co.com.crediya.usecase.registerloanapplication;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationRegisterInvalidDataException;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loanapplicationstate.exceptions.LoanApplicationStateNotFoundException;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.exceptions.LoanTypeNotFoundException;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.UserNotFoundException;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class RegisterLoanApplicationUseCase {

    private static final String PENDING_REVIEW_STATE_CODE = "PENDING_REVIEW";
    private static final String ACCEPTED_STATE_CODE = "ACCEPTED";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final LoanApplicationStateRepository  loanApplicationStateRepository;
    private final UserRepository userRepository;
    private final LoanApplicationNotifier loanApplicationNotifier;

    public Mono<LoanApplication> registerLoanApplication(LoanApplication loanApplication) {
       return Mono.deferContextual(ctx -> this.validateLoanApplication(loanApplication)
               .then(Mono.defer(() -> this.validateExistingLoanType(loanApplication.getLoanType().getId())))
               .then(Mono.defer(() -> this.findLoanApplicationState(PENDING_REVIEW_STATE_CODE)))
               .map(state -> this.setLoanApplicationState(loanApplication, state))
               .flatMap(this.loanApplicationRepository::save)
               .doOnSuccess(saveLoanApplication -> this.calculateDebtCapacity(saveLoanApplication)
                       .subscribeOn(Schedulers.boundedElastic())
                       .contextWrite(Context.of(ctx))
                       .subscribe()
               ));
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

    private Mono<LoanApplicationState> findLoanApplicationState(String stateCode) {
        final var errorMessage = "Loan application state not found by the following code %s"
                .formatted(stateCode);

        return this.loanApplicationStateRepository.findByCode(stateCode)
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

    private Mono<Void> calculateDebtCapacity(LoanApplication loanApplication) {
        return this.getLoanType(loanApplication.getLoanType().getId())
                .filter(LoanType::getIsAutoValidationEnabled)
                .doOnNext(loanApplication::setLoanType)
                .flatMap(type -> Mono.zip(
                        this.getUserBaseSalary(loanApplication.getEmail()),
                        this.getActiveLoans(loanApplication.getEmail())
                ))
                .flatMap(t -> this.loanApplicationNotifier.notifyDebtCapacityCalculation(t.getT2(), t.getT1(), loanApplication));
    }

    private Mono<BigDecimal> getUserBaseSalary(String email) {
        return this.userRepository.findByEmail(email)
                .map(User::getBaseSalary)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User with email %s not found.".formatted(email))))
                .subscribeOn(Schedulers.parallel());
    }

    private Mono<List<LoanApplication>> getActiveLoans(String email) {
        return this.findLoanApplicationState(ACCEPTED_STATE_CODE)
                .flatMapMany(st -> this.loanApplicationRepository.findByStateIdAndEmail(st.getId(), email))
                .flatMap(this::setLoanType)
                .collectList()
                .subscribeOn(Schedulers.parallel());
    }

    private Mono<LoanApplication> setLoanType(LoanApplication loanApplication) {
        return this.loanTypeRepository.findById(loanApplication.getLoanType().getId())
                .map(type -> loanApplication.toBuilder().loanType(type).build());
    }

    private Mono<LoanType> getLoanType(Long loanTypeId) {
        return this.loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException("Loan type not found.")));
    }
}
