package co.com.crediya.r2dbc.repositories;

import co.com.crediya.r2dbc.entities.LoanApplicationStateEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanApplicationStateReactiveRepository extends ReactiveCrudRepository<LoanApplicationStateEntity, Long> {
    Mono<LoanApplicationStateEntity> findByCode(String code);
}
