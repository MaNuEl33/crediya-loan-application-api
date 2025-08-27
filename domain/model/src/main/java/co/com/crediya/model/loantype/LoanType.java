package co.com.crediya.model.loantype;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class LoanType {
    private Long id;
    private String name;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private BigDecimal interestRate;
    private Boolean isAutoValidationEnabled;
}
