package co.com.crediya.model.manualvalidationapplicationreport;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ManualValidationApplicationReportElement {
    private Long applicationId;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String loanType;
    private BigDecimal interestRate;
    private String applicationState;
}
