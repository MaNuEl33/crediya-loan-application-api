package co.com.crediya.sqs.listener;

import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationCodeState;
import co.com.crediya.usecase.updateautomaticvalidationloanapplicationstate.UpdateAutomaticValidationLoanApplicationStateUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final UpdateAutomaticValidationLoanApplicationStateUseCase updateAutomaticValidationLoanApplicationStateUseCase;

    @Override
    public Mono<Void> apply(Message message) {
        log.info("A state update for a automatic validation loan application has been received.");

        final var bodyMessage = message.body();

        final var objectMapper = new ObjectMapper();

        final JsonNode root;
        final Long loanApplicationId;
        final LoanApplicationCodeState codeState;

        try {
            root = objectMapper.readTree(bodyMessage);
            loanApplicationId = root.get("loanApplicationId").asLong();
            codeState = objectMapper.treeToValue(root.get("state"), LoanApplicationCodeState.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message", e);
            return Mono.error(e);
        }

        return this.updateAutomaticValidationLoanApplicationStateUseCase.execute(loanApplicationId, codeState)
                .doOnSuccess(v -> log.info("The loan application has been updated successfully."))
                .doOnError(err -> log.error("An error occurred while updating the loan application state", err));
    }
}
