package co.com.crediya.model.loanapplicationstate.gateways;

import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import reactor.core.publisher.Mono;

public interface LoanApplicationStateRepository {
    Mono<LoanApplicationState> findByCode(String code);
}
