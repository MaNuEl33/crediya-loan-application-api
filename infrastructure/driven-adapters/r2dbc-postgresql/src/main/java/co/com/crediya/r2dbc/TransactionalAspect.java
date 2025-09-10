package co.com.crediya.r2dbc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class TransactionalAspect {

    private final TransactionalOperator transactionalOperator;

    public TransactionalAspect(R2dbcTransactionManager transactionManager) {
        this.transactionalOperator = TransactionalOperator.create(transactionManager);
    }

    @Around("@annotation(co.com.crediya.usecase.UseCaseTransactional)")
    public Object wrapInTransaction(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();

        if (result instanceof Mono<?>) {
            return this.transactionalOperator.transactional((Mono<?>) result);
        } else if (result instanceof Flux<?>) {
            return this.transactionalOperator.transactional((Flux<?>) result);
        }

        throw new IllegalArgumentException("@UseCaseTransactional only supports Mono or Flux");
    }
}
