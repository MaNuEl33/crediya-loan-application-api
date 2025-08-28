package co.com.crediya.r2dbc;

import co.com.crediya.model.loanapplicationstate.LoanApplicationState;
import co.com.crediya.model.loanapplicationstate.gateways.LoanApplicationStateRepository;
import co.com.crediya.r2dbc.mappers.LoanApplicationStateEntityMapper;
import co.com.crediya.r2dbc.repositories.LoanApplicationStateReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
@RequiredArgsConstructor
public class LoanApplicationStateRepositoryAdapter implements LoanApplicationStateRepository {

    private final LoanApplicationStateReactiveRepository reactiveRepository;
    private final LoanApplicationStateEntityMapper entityMapper;

    @Override
    public Mono<LoanApplicationState> findByCode(String code) {
        return this.reactiveRepository.findByCode(code)
                .map(entityMapper::toLoanApplicationState)
                .doFirst(() -> log.info("Finding loan application state by code in the database."))
                .doOnSuccess(l -> log.info("Loan application state found in the database."))
                .doOnError(err -> log.error("Error finding loan application state in the database.", err));
    }
}
