package co.com.crediya.model.loanapplicationstate.gateways;

import co.com.crediya.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanApplicationStateRepository {
    Mono<LoanType> findByCode(String code);
}
