package co.com.crediya.usecase.manualvalidationapplicationreport;

import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReport;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportFilter;
import co.com.crediya.model.manualvalidationapplicationreport.exceptions.ManualValidationApplicationReportInvalidDataException;
import co.com.crediya.model.manualvalidationapplicationreport.gateways.ManualValidationApplicationReportRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ManualValidationApplicationReportUseCase {

    private final ManualValidationApplicationReportRepository repository;

    public Mono<ManualValidationApplicationReport> getManualValidationApplicationReport(
            ManualValidationApplicationReportFilter filter, int pageNumber, int pageSize) {
        return this.validPageAttributes(pageNumber, pageSize)
                .then(Mono.defer(() -> this.repository.getManualValidationApplicationReport(filter, pageNumber, pageSize)));
    }

    private Mono<Void> validPageAttributes(int pageNumber, int pageSize) {
        return Mono.defer(() ->
                this.validatePageNumber(pageNumber).then(this.validatePageSize(pageSize)));
    }

    private Mono<Void> validatePageNumber(int pageNumber) {
        if (pageNumber < 1) {
            return Mono.error(new ManualValidationApplicationReportInvalidDataException("The page number must be greater than 0."));
        }

        return Mono.empty();
    }

    private Mono<Void> validatePageSize(int pageSize) {
        if (pageSize < 1) {
            return Mono.error(new ManualValidationApplicationReportInvalidDataException("The page size must be greater than 0."));
        }

        return Mono.empty();
    }
}
