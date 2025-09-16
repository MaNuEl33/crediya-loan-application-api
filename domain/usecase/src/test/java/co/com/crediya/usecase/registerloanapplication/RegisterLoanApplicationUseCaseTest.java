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
import co.com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RegisterLoanApplicationUseCaseTest {

    private static final String EMAIL = "manuelharo1994@gmail.com";

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private LoanApplicationStateRepository loanApplicationStateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoanApplicationNotifier loanApplicationNotifier;

    @InjectMocks
    private RegisterLoanApplicationUseCase useCase;

    @Test
    void shouldRegisterLoanApplicationSuccessfully() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        final var loanApplicationToSave = loanApplication.toBuilder()
                .state(LoanApplicationState.builder().id(15L).build())
                .build();

        final var loanApplicationSaved = loanApplicationToSave.toBuilder()
                .id(145L)
                .build();

        Mockito.when(this.loanTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(LoanType.builder().id(9L).isAutoValidationEnabled(false).build()));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.just(LoanApplicationState.builder().id(15L).build()));

        Mockito.when(this.loanApplicationRepository.save(Mockito.any(LoanApplication.class)))
                .thenReturn(Mono.just(loanApplicationSaved));

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .expectNextCount(1L)
                .verifyComplete();

        Mockito.verify(this.loanTypeRepository, Mockito.timeout(2000).times(2)).findById(9L);
        Mockito.verify(this.loanApplicationStateRepository).findByCode("PENDING_REVIEW");
        Mockito.verify(this.loanApplicationRepository).save(loanApplicationToSave);

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository);

        Mockito.verifyNoInteractions(this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldCalculateDebtCapacityWhenAutoValidationLoanTypeIsEnabled() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        final var loanApplicationToSave = loanApplication.toBuilder()
                .state(LoanApplicationState.builder().id(15L).build())
                .build();

        final var loanApplicationSaved = loanApplicationToSave.toBuilder()
                .id(145L)
                .build();

        final var user =  User.builder()
                .email(EMAIL)
                .baseSalary(BigDecimal.valueOf(10000))
                .build();

        final var activeLoanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(200))
                .term(12)
                .email(EMAIL)
                .loanType(LoanType.builder().id(2L).build())
                .state(LoanApplicationState.builder().id(10L).build())
                .build();

        final var loanApplicationToCalculate = loanApplicationSaved.toBuilder()
                .loanType(LoanType.builder().id(9L).interestRate(BigDecimal.valueOf(0.02)).isAutoValidationEnabled(true).build())
                .build();

        final var activeLoanApplicationToCalculate = activeLoanApplication.toBuilder()
                .loanType(LoanType.builder().id(2L).interestRate(BigDecimal.valueOf(0.01)).build())
                .build();

        Mockito.when(this.loanTypeRepository.findById(9L))
                .thenReturn(Mono.just(LoanType.builder().id(9L).interestRate(BigDecimal.valueOf(0.02)).isAutoValidationEnabled(true).build()));

        Mockito.when(this.loanApplicationStateRepository.findByCode("PENDING_REVIEW"))
                .thenReturn(Mono.just(LoanApplicationState.builder().id(15L).build()));

        Mockito.when(this.loanApplicationRepository.save(Mockito.any(LoanApplication.class)))
                .thenReturn(Mono.just(loanApplicationSaved));

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Mono.just(user));

        Mockito.when(this.loanApplicationStateRepository.findByCode("ACCEPTED"))
                .thenReturn(Mono.just(LoanApplicationState.builder().id(10L).build()));

        Mockito.when(this.loanApplicationRepository.findByStateIdAndEmail(Mockito.anyLong(), Mockito.anyString()))
                .thenReturn(Flux.fromIterable(List.of(activeLoanApplication)));

        Mockito.when(this.loanTypeRepository.findById(2L))
                .thenReturn(Mono.just(LoanType.builder().id(2L).interestRate(BigDecimal.valueOf(0.01)).build()));

        Mockito.when(this.loanApplicationNotifier.notifyDebtCapacityCalculation(Mockito.anyList(), Mockito.any(BigDecimal.class), Mockito.any(LoanApplication.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .expectNextCount(1L)
                .verifyComplete();

        Mockito.verify(this.loanTypeRepository, Mockito.timeout(2000).times(2)).findById(9L);
        Mockito.verify(this.loanApplicationStateRepository).findByCode("PENDING_REVIEW");
        Mockito.verify(this.loanApplicationRepository).save(loanApplicationToSave);
        Mockito.verify(this.userRepository, Mockito.timeout(2000)).findByEmail(EMAIL);
        Mockito.verify(this.loanApplicationStateRepository, Mockito.timeout(2000)).findByCode("ACCEPTED");
        Mockito.verify(this.loanApplicationRepository, Mockito.timeout(2000)).findByStateIdAndEmail(10L, EMAIL);
        Mockito.verify(this.loanTypeRepository, Mockito.timeout(2000)).findById(2L);
        Mockito.verify(this.loanApplicationNotifier, Mockito.timeout(2000)).notifyDebtCapacityCalculation(List.of(activeLoanApplicationToCalculate), BigDecimal.valueOf(10000), loanApplicationToCalculate);

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenTheObjectIsNull() {
        StepVerifier.create(this.useCase.registerLoanApplication(null))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenAmountIsNull() {
        final var loanApplication = LoanApplication.builder()
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenAmountIsNegative() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(-5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenTermIsNull() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenTermIsNegative() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(-6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenEmailIsNull() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenEmailIsInvalid() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email("manuelharo1994")
                .loanType(LoanType.builder().id(9L).build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenLoanTypeIsNull() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenLoanTypeIdIsNull() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().build())
                .build();

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenLoanTypeNotExists() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        Mockito.when(this.loanTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanTypeNotFoundException.class);

        Mockito.verify(this.loanTypeRepository).findById(9L);

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository);
        Mockito.verifyNoInteractions(this.loanApplicationStateRepository,  this.loanApplicationRepository,
                this.userRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenLoanApplicationStateNotExists() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        Mockito.when(this.loanTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(LoanType.builder().id(9L).build()));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationStateNotFoundException.class);

        Mockito.verify(this.loanTypeRepository).findById(9L);
        Mockito.verify(this.loanApplicationStateRepository).findByCode("PENDING_REVIEW");

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository, this.loanApplicationStateRepository);
        Mockito.verifyNoInteractions(this.loanApplicationRepository, this.userRepository, this.loanApplicationNotifier);
    }
}
