package co.com.crediya.usecase.manualvalidationapplicationreport;

import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReport;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportElement;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportFilter;
import co.com.crediya.model.manualvalidationapplicationreport.exceptions.ManualValidationApplicationReportInvalidDataException;
import co.com.crediya.model.manualvalidationapplicationreport.gateways.ManualValidationApplicationReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ManualValidationApplicationReportUseCaseTest {

    @Mock
    private ManualValidationApplicationReportRepository repository;

    @InjectMocks
    private ManualValidationApplicationReportUseCase useCase;

    @Test
    void shouldGetManualValidationApplicationReport() {
        final var filter = ManualValidationApplicationReportFilter.builder()
                .applicationStateId(1L)
                .loanTypeId(2L)
                .email("manuelharo1994@gmail.com")
                .term(6)
                .build();

        final var report = ManualValidationApplicationReport.builder()
                .applications(List.of(ManualValidationApplicationReportElement.builder()
                        .applicationId(1L)
                        .applicationState("Pendiente")
                        .amount(BigDecimal.valueOf(2000))
                        .term(6)
                        .email("manuelharo1994@gmail.com")
                        .interestRate(BigDecimal.valueOf(0.01))
                        .loanType("Tipo 1")
                        .build()))
                .build();

        final var expectedReport = report.toBuilder().build();
        final var expectedFilter = filter.toBuilder().build();

        Mockito.when(this.repository.getManualValidationApplicationReport(
                Mockito.any(ManualValidationApplicationReportFilter.class), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Mono.just(report));

        StepVerifier.create(this.useCase.getManualValidationApplicationReport(filter, 1, 20))
                .expectNext(expectedReport)
                .verifyComplete();

        Mockito.verify(this.repository).getManualValidationApplicationReport(expectedFilter, 1, 20);
        Mockito.verifyNoMoreInteractions(this.repository);
    }

    @Test
    void shouldNotGetManualValidationApplicationReportWhenPageNumberIsLessThanOrEqualToZero() {
        StepVerifier.create(this.useCase.getManualValidationApplicationReport(
                ManualValidationApplicationReportFilter.builder().build(), 0, 20))
                .verifyError(ManualValidationApplicationReportInvalidDataException.class);

        Mockito.verifyNoInteractions(this.repository);
    }

    @Test
    void shouldNotGetManualValidationApplicationReportWhenPageSizeIsLessThanOrEqualToZero() {
        StepVerifier.create(this.useCase.getManualValidationApplicationReport(
                        ManualValidationApplicationReportFilter.builder().build(), 1, 0))
                .verifyError(ManualValidationApplicationReportInvalidDataException.class);

        Mockito.verifyNoInteractions(this.repository);
    }
}
