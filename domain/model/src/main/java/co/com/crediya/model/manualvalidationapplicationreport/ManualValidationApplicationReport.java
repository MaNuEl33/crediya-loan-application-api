package co.com.crediya.model.manualvalidationapplicationreport;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ManualValidationApplicationReport {
    private List<ManualValidationApplicationReportElement> applications;
    private long totalApplications;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    private boolean isFirstPage;
    private boolean isLastPage;
    private boolean isEmpty;
}
