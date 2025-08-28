package co.com.crediya.r2dbc.repositories;

import co.com.crediya.r2dbc.entities.LoanApplicationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanApplicationReactiveRepository extends ReactiveCrudRepository<LoanApplicationEntity, Long> {
}
