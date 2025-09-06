package co.com.crediya.model.manualvalidationapplicationreport;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ManualValidationApplicationReportFilter {
    private Integer term;
    private String email;
    private Long loanTypeId;
    private Long applicationStateId;
}
