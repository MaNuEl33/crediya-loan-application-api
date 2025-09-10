package co.com.crediya.usecase.approveorrejectloanapplication;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.exceptions.LoanApplicationNotFoundException;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import co.com.crediya.model.loanapplicationstate.exceptions.ApproveOrRejectLoanApplicationInvalidDataException;
import co.com.crediya.model.loanapplicationstate.exceptions.LoanApplicationStateNotFoundException;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.model.loantype.exceptions.LoanTypeAlreadyProcessedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ApproveOrRejectLoanApplicationUseCaseTest {

    private static final String APPROVED_STATE_CODE = "ACCEPTED";
    private static final String REJECTED_STATE_CODE = "REJECTED";

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanApplicationStateRepository loanApplicationStateRepository;

    @Mock
    private  LoanApplicationNotifier loanApplicationNotifier;

    @InjectMocks
    private ApproveOrRejectLoanApplicationUseCase useCase;

    @ParameterizedTest
    @EnumSource(LoanApplicationApprovalState.class)
    void shouldApproveOrRejectLoanApplicationSuccessfully(LoanApplicationApprovalState approvalState) {
        final var loanApplication = LoanApplication.builder()
                .id(1L)
                .state(LoanApplicationState.builder().id(3L).build())
                .build();

        final var currentLoanApplicationState = LoanApplicationState.builder()
                .id(3L)
                .code("MANUAL_REVIEW")
                .build();

        final var resolvedLoanApplicationState = switch (approvalState) {
            case APPROVED -> LoanApplicationState.builder().id(2L).code(APPROVED_STATE_CODE).build();
            case REJECTED -> LoanApplicationState.builder().id(4L).code(REJECTED_STATE_CODE).build();
        };

        final var resolvedApplicationStateCode = switch (approvalState) {
            case APPROVED -> APPROVED_STATE_CODE;
            case REJECTED -> REJECTED_STATE_CODE;
        };

        final var loanApplicationToSave = loanApplication.toBuilder()
                .state(resolvedLoanApplicationState.toBuilder().build())
                .build();

        final var loanApplicationSaved = loanApplicationToSave.toBuilder().build();

        final var loanApplicationToNotify = loanApplicationSaved.toBuilder().build();

        Mockito.when(this.loanApplicationRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(loanApplication));

        Mockito.when(this.loanApplicationStateRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(currentLoanApplicationState));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.just(resolvedLoanApplicationState));

        Mockito.when(this.loanApplicationRepository.save(Mockito.any(LoanApplication.class)))
                .thenReturn(Mono.just(loanApplicationSaved));

        Mockito.when(this.loanApplicationNotifier.notifyProcessedLoanApplication(
                    Mockito.any(LoanApplication.class), Mockito.any(LoanApplicationApprovalState.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(1L, approvalState))
                .verifyComplete();

        Mockito.verify(this.loanApplicationRepository).findById(1L);
        Mockito.verify(this.loanApplicationStateRepository).findById(3L);
        Mockito.verify(this.loanApplicationStateRepository).findByCode(resolvedApplicationStateCode);
        Mockito.verify(this.loanApplicationRepository).save(loanApplicationToSave);
        Mockito.verify(this.loanApplicationNotifier)
                .notifyProcessedLoanApplication(loanApplicationToNotify, approvalState);

        Mockito.verifyNoMoreInteractions(this.loanApplicationRepository, this.loanApplicationStateRepository,
                this.loanApplicationNotifier);
    }

    @Test
    void shouldNotApproveOrRejectLoanApplicationWhenLoanApplicationIdIsNull() {
        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(null,
                LoanApplicationApprovalState.APPROVED))
                .verifyError(ApproveOrRejectLoanApplicationInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanApplicationRepository, this.loanApplicationStateRepository,
                this.loanApplicationNotifier);
    }

    @Test
    void shouldNotApprovedOrRejectLoanApplicationWhenLoanApplicationApprovalStateIsNull() {
        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(1L, null))
                .verifyError(ApproveOrRejectLoanApplicationInvalidDataException.class);

        Mockito.verifyNoInteractions(this.loanApplicationRepository, this.loanApplicationStateRepository,
                this.loanApplicationNotifier);
    }

    @Test
    void shouldNotApprovedOrRejectLoanApplicationWhenLoanApplicationDoesNotExist() {
        Mockito.when(this.loanApplicationRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(1L,
                        LoanApplicationApprovalState.APPROVED))
                .verifyError(LoanApplicationNotFoundException.class);

        Mockito.verify(this.loanApplicationRepository).findById(1L);

        Mockito.verifyNoMoreInteractions(this.loanApplicationRepository);
        Mockito.verifyNoInteractions(this.loanApplicationStateRepository, this.loanApplicationNotifier);
    }

    @Test
    void shouldNotApprovedOrRejectLoanApplicationWhenCurrentLoanApplicationStatusDoesNotExist() {
        final var loanApplication = LoanApplication.builder()
                .id(1L)
                .state(LoanApplicationState.builder().id(3L).build())
                .build();

        Mockito.when(this.loanApplicationRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(loanApplication));

        Mockito.when(this.loanApplicationStateRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(1L,
                LoanApplicationApprovalState.APPROVED))
                .verifyError(LoanApplicationStateNotFoundException.class);

        Mockito.verify(this.loanApplicationRepository).findById(1L);
        Mockito.verify(this.loanApplicationStateRepository).findById(3L);

        Mockito.verifyNoMoreInteractions(this.loanApplicationRepository,  this.loanApplicationStateRepository);
        Mockito.verifyNoInteractions(this.loanApplicationNotifier);
    }

    @ParameterizedTest
    @ValueSource(strings = { APPROVED_STATE_CODE, REJECTED_STATE_CODE })
    void shouldNotApprovedOrRejectLoanApplicationWhenCurrentLoanApplicationIsAlreadyApprovedOrRejected(String currentStateCode) {
        final var loanApplication = LoanApplication.builder()
                .id(1L)
                .state(LoanApplicationState.builder().id(3L).build())
                .build();

        final var currentLoanApplicationState = LoanApplicationState.builder()
                .id(3L)
                .code(currentStateCode)
                .build();

        Mockito.when(this.loanApplicationRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(loanApplication));

        Mockito.when(this.loanApplicationStateRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(currentLoanApplicationState));

        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(1L,
                        LoanApplicationApprovalState.APPROVED))
                .verifyError(LoanTypeAlreadyProcessedException.class);

        Mockito.verify(this.loanApplicationRepository).findById(1L);
        Mockito.verify(this.loanApplicationStateRepository).findById(3L);

        Mockito.verifyNoMoreInteractions(this.loanApplicationRepository,  this.loanApplicationStateRepository);
        Mockito.verifyNoInteractions(this.loanApplicationNotifier);
    }

    @ParameterizedTest
    @EnumSource(LoanApplicationApprovalState.class)
    void shouldNotApprovedOrRejectLoanApplicationWhenLoanApplicationStatusToSaveDoesNotExist(LoanApplicationApprovalState approvalState) {
        final var loanApplication = LoanApplication.builder()
                .id(1L)
                .state(LoanApplicationState.builder().id(3L).build())
                .build();

        final var currentLoanApplicationState = LoanApplicationState.builder()
                .id(3L)
                .code("MANUAL_REVIEW")
                .build();

        final var resolvedApplicationStateCode = switch (approvalState) {
            case APPROVED -> APPROVED_STATE_CODE;
            case REJECTED -> REJECTED_STATE_CODE;
        };

        Mockito.when(this.loanApplicationRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(loanApplication));

        Mockito.when(this.loanApplicationStateRepository.findById(Mockito.anyLong()))
                .thenReturn(Mono.just(currentLoanApplicationState));

        Mockito.when(this.loanApplicationStateRepository.findByCode(Mockito.anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(this.useCase.approveOrRejectLoanApplication(1L, approvalState))
                .verifyError(LoanApplicationStateNotFoundException.class);

        Mockito.verify(this.loanApplicationRepository).findById(1L);
        Mockito.verify(this.loanApplicationStateRepository).findById(3L);
        Mockito.verify(this.loanApplicationStateRepository).findByCode(resolvedApplicationStateCode);

        Mockito.verifyNoMoreInteractions(this.loanApplicationRepository, this.loanApplicationStateRepository);
        Mockito.verifyNoInteractions(this.loanApplicationNotifier);
    }
}
