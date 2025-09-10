package co.com.crediya.r2dbc;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.crediya.r2dbc.mappers.LoanApplicationEntityMapper;
import co.com.crediya.r2dbc.repositories.LoanApplicationReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Log4j2
@RequiredArgsConstructor
public class LoanApplicationRepositoryAdapter implements LoanApplicationRepository {

    private final LoanApplicationReactiveRepository reactiveRepository;
    private final LoanApplicationEntityMapper entityMapper;

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        log.info("Saving loan application in the database.");

        final var loanApplicationEntity = this.entityMapper.toLoadApplicationEntity(loanApplication);

        return this.reactiveRepository.save(loanApplicationEntity)
                .map(this.entityMapper::toLoadApplicationModel)
                .doOnSuccess(l -> log.info("Loan application saved in the database successfully."))
                .doOnError(e -> log.error("Error saving the loan application in the database.", e));
    }

    @Override
    public Mono<LoanApplication> findById(Long id) {
        return this.reactiveRepository.findById(id)
                .map(this.entityMapper::toLoadApplicationModel)
                .doFirst(() -> log.info("Searching the loan application by its id in the database."))
                .doOnSuccess(r -> log.info("Loan application found in the database."))
                .doOnError(err -> log.error("Error finding the loan application in the database.", err));
    }
}
