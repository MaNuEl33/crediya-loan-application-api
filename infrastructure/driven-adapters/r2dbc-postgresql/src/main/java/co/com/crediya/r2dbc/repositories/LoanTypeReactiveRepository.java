package co.com.crediya.r2dbc.repositories;

import co.com.crediya.r2dbc.entities.LoanTypeEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanTypeReactiveRepository extends ReactiveCrudRepository<LoanTypeEntity, Long> {
}
