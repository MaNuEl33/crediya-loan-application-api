package co.com.crediya.model.loanapplicationstate;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class LoanApplicationState {
    private Long id;
    private String code;
    private String name;
    private String description;
}
