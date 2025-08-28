package co.com.crediya.r2dbc;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.r2dbc.mappers.LoanTypeEntityMapper;
import co.com.crediya.r2dbc.repositories.LoanTypeReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {

    private final LoanTypeReactiveRepository reactiveRepository;
    private final LoanTypeEntityMapper entityMapper;

    @Override
    public Mono<LoanType> findById(Long id) {
        return this.reactiveRepository.findById(id)
                .map(entityMapper::toLoanType)
                .doFirst(() -> log.info("Finding loan type by id in the database."))
                .doOnSuccess(l -> log.info("Loan type found in the database."))
                .doOnError(err -> log.error("Error finding loan type in the database.", err));
    }
}
