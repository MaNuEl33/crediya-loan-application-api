package co.com.crediya.model.manualvalidationapplicationreport.gateways;

import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReport;
import co.com.crediya.model.manualvalidationapplicationreport.ManualValidationApplicationReportFilter;
import reactor.core.publisher.Mono;

public interface ManualValidationApplicationReportRepository {
    Mono<ManualValidationApplicationReport> getManualValidationApplicationReport(
            ManualValidationApplicationReportFilter filter, int pageNumber, int pageSize);
}
