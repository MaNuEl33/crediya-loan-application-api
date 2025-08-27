package co.com.crediya.model.loanapplication;
import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loantype.LoanType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class LoanApplication {
    private Long id;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private LoanApplicationState state;
    private LoanType loanType;
}
