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
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.exceptions.UserNotFoundException;
import co.com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

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

        final var loanApplicationToSave = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .state(LoanApplicationState.builder().id(15L).build())
                .build();

        Mockito.when(this.loanTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(LoanType.builder().id(9L).build()));

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Mono.just(User.builder().build()));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.just(LoanApplicationState.builder().id(15L).build()));

        Mockito.when(this.loanApplicationRepository.save(Mockito.any(LoanApplication.class)))
                .thenReturn(Mono.just(LoanApplication.builder().build()));

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .expectNextCount(1L)
                .verifyComplete();

        Mockito.verify(this.loanTypeRepository).findById(9L);
        Mockito.verify(this.userRepository).findByEmail(EMAIL);
        Mockito.verify(this.loanApplicationStateRepository).findByCode("PENDING_REVIEW");
        Mockito.verify(this.loanApplicationRepository).save(loanApplicationToSave);

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenTheObjectIsNull() {
        StepVerifier.create(this.useCase.registerLoanApplication(null))
                .verifyError(LoanApplicationRegisterInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanTypeRepository, this.loanApplicationStateRepository,
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.loanApplicationRepository, this.userRepository);
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
                this.userRepository);
    }

    @Test
    void shouldNotRegisterLoanApplicationWhenUserNotExists() {
        final var loanApplication = LoanApplication.builder()
                .amount(BigDecimal.valueOf(5000))
                .term(6)
                .email(EMAIL)
                .loanType(LoanType.builder().id(9L).build())
                .build();

        Mockito.when(this.loanTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(LoanType.builder().id(9L).build()));

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(UserNotFoundException.class);

        Mockito.verify(this.loanTypeRepository).findById(9L);
        Mockito.verify(this.userRepository).findByEmail(EMAIL);

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository, this.userRepository);
        Mockito.verifyNoInteractions(this.loanApplicationStateRepository,  this.loanApplicationRepository);
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

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Mono.just(User.builder().build()));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.registerLoanApplication(loanApplication))
                .verifyError(LoanApplicationStateNotFoundException.class);

        Mockito.verify(this.loanTypeRepository).findById(9L);
        Mockito.verify(this.userRepository).findByEmail(EMAIL);
        Mockito.verify(this.loanApplicationStateRepository).findByCode("PENDING_REVIEW");

        Mockito.verifyNoMoreInteractions(this.loanTypeRepository, this.userRepository,
                this.loanApplicationStateRepository);
        Mockito.verifyNoInteractions(this.loanApplicationRepository);
    }
}
