package co.com.crediya.usecase.updateautomaticvalidationloanapplicationstate;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationCodeState;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class UpdateAutomaticValidationLoanApplicationStateUseCaseTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApplicationStateRepository loanApplicationStateRepository;

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private LoanApplicationNotifier loanApplicationNotifier;

    @InjectMocks
    private UpdateAutomaticValidationLoanApplicationStateUseCase useCase;

    @ParameterizedTest
    @EnumSource(LoanApplicationCodeState.class)
    void shouldUpdateLoanApplicationState(LoanApplicationCodeState loanApplicationCodeState) {
        final var loanApplication = LoanApplication.builder()
                .id(5L)
                .email("manuelharo1994@gmail.com")
                .amount(BigDecimal.valueOf(200))
                .term(6)
                .state(LoanApplicationState.builder().id(14L).build())
                .loanType(LoanType.builder().id(12L).build())
                .build();

        final var loanApplicationToSave = loanApplication.toBuilder()
                .state(LoanApplicationState.builder().id(9L).code(loanApplicationCodeState.name()).build())
                .build();

        final var loanApplicationSaved = loanApplicationToSave.toBuilder()
                .state(LoanApplicationState.builder().id(9L).build())
                .build();

        final var loanApplicationToNotify = loanApplicationSaved.toBuilder()
                .loanType(LoanType.builder().id(12L).interestRate(BigDecimal.valueOf(0.01)).build())
                .build();

        Mockito.when(this.loanApplicationRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(loanApplication));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.just(LoanApplicationState.builder().id(9L).code(loanApplicationCodeState.name()).build()));

        Mockito.when(this.loanApplicationRepository.save(Mockito.any(LoanApplication.class)))
                .thenReturn(Mono.just(loanApplicationSaved));

        Mockito.when(this.loanTypeRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(LoanType.builder().id(12L).interestRate(BigDecimal.valueOf(0.01)).build()));

        Mockito.when(this.loanApplicationNotifier.notifyAutomaticValidationLoanApplicationState(Mockito.any(LoanApplication.class), Mockito.any(LoanApplicationCodeState.class)))
                .thenReturn(Mono.empty());

        if (LoanApplicationCodeState.ACCEPTED.equals(loanApplicationCodeState)) {
            Mockito.when(this.loanApplicationNotifier.notifyAcceptedLoanApplication(Mockito.any(LoanApplication.class)))
                    .thenReturn(Mono.empty());
        }

        StepVerifier.create(this.useCase.execute(5L, loanApplicationCodeState))
                .verifyComplete();

        Mockito.verify(this.loanApplicationRepository).findById(5L);
        Mockito.verify(this.loanApplicationStateRepository).findByCode(loanApplicationCodeState.name());
        Mockito.verify(this.loanApplicationRepository).save(loanApplicationToSave);
        Mockito.verify(this.loanTypeRepository).findById(12L);
        Mockito.verify(this.loanApplicationNotifier).notifyAutomaticValidationLoanApplicationState(loanApplicationToNotify, loanApplicationCodeState);

        if (LoanApplicationCodeState.ACCEPTED.equals(loanApplicationCodeState)) {
            Mockito.verify(this.loanApplicationNotifier).notifyAcceptedLoanApplication(loanApplicationToNotify);
        }

        Mockito.verifyNoMoreInteractions(this.loanApplicationRepository, this.loanApplicationStateRepository,
                this.loanTypeRepository, this.loanApplicationNotifier);
    }
}
