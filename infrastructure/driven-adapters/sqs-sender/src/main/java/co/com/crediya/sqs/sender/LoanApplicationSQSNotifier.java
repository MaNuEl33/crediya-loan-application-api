package co.com.crediya.sqs.sender;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationCodeState;
import co.com.crediya.sqs.sender.config.SQSSenderProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoanApplicationSQSNotifier implements LoanApplicationNotifier {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    @Override
    public Mono<Void> notifyProcessedLoanApplication(LoanApplication loanApplication, LoanApplicationApprovalState approvalState) {
        log.info("Sending processed loan application notification to SQS");

        final SendMessageRequest messageRequest;
        try {
            messageRequest = this.buildMessageRequest(loanApplication, approvalState);
        } catch (JsonProcessingException e) {
            this.logSerializingError(e);
            return Mono.error(e);
        }

        return Mono.fromFuture(this.client.sendMessage(messageRequest))
                .doOnNext(this::logSendMessageResponse)
                .doOnSuccess(r -> log.info("Processed loan application notification sent to SQS successfully."))
                .doOnError(err ->  log.error("Error sending processed loan application notification to SQS", err))
                .then();
    }

    @Override
    public Mono<Void> notifyDebtCapacityCalculation(List<LoanApplication> activeLoans, BigDecimal baseSalary, LoanApplication loanApplication) {
        log.info("Sending debt capacity calculation notification to SQS");

        final SendMessageRequest messageRequest;

        try {
            messageRequest = this.buildMessageRequest(activeLoans, baseSalary, loanApplication);
        } catch (JsonProcessingException e) {
            this.logSerializingError(e);
            return Mono.error(e);
        }

        return Mono.fromFuture(this.client.sendMessage(messageRequest))
                .doOnNext(this::logSendMessageResponse)
                .doOnSuccess(r -> log.info("Debt capacity calculation notification sent to SQS successfully."))
                .doOnError(err -> log.error("Error sending debt capacity calculation notification to SQS", err))
                .then();

    }

    @Override
    public Mono<Void> notifyAutomaticValidationLoanApplicationState(LoanApplication loanApplication, LoanApplicationCodeState codeState) {
        log.info("Sending automatic validation loan application state notification to SQS");

        final SendMessageRequest messageRequest;

        try {
            messageRequest = this.buildMessageRequest(loanApplication, codeState);
        } catch (JsonProcessingException e) {
            this.logSerializingError(e);
            return Mono.error(e);
        }

        return Mono.fromFuture(this.client.sendMessage(messageRequest))
                .doOnNext(this::logSendMessageResponse)
                .doOnSuccess(r -> log.info("Automatic validation loan application state notification sent to SQS successfully."))
                .doOnError(err -> log.error("Error sending automatic validation loan application state notification to SQS", err))
                .then();
    }

    private SendMessageRequest buildMessageRequest(LoanApplication loanApplication, LoanApplicationApprovalState approvalState)
            throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        final var root = objectMapper.createObjectNode();
        root.put("id", loanApplication.getId());
        root.put("email", loanApplication.getEmail());
        root.put("amount", loanApplication.getAmount().toPlainString());
        root.put("term", loanApplication.getTerm());
        root.put("status", approvalState.name());

        final var message = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        return SendMessageRequest.builder()
                .queueUrl(this.properties.processedLoanApplicationsQueueUrl())
                .messageBody(message)
                .build();
    }

    private SendMessageRequest buildMessageRequest(List<LoanApplication> activeLoans, BigDecimal baseSalary, LoanApplication loanApplication)
            throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        final var root = objectMapper.createObjectNode();
        root.set("activeLoans", objectMapper.valueToTree(activeLoans));
        root.put("baseSalary", baseSalary);
        root.set("loanApplication", objectMapper.valueToTree(loanApplication));

        final var message = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        return SendMessageRequest.builder()
                .queueUrl(this.properties.debtCapacityCalculationQueueUrl())
                .messageBody(message)
                .build();
    }

    private SendMessageRequest buildMessageRequest(LoanApplication loanApplication,  LoanApplicationCodeState codeState)
            throws JsonProcessingException {
        final var objectMapper = new ObjectMapper();

        final var root = objectMapper.createObjectNode();
        root.set("loanApplication", objectMapper.valueToTree(loanApplication));
        root.set("state", objectMapper.valueToTree(codeState));

        final var message = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        return SendMessageRequest.builder()
                .queueUrl(this.properties.processedAutomaticValidationLoanApplicationsQueueUrl())
                .messageBody(message)
                .build();
    }

    private void logSendMessageResponse(SendMessageResponse sendMessageResponse) {
        log.debug("Message sent {}", sendMessageResponse.messageId());
    }

    private void logSerializingError(JsonProcessingException e) {
        log.error("Error serializing the message request to SQS", e);
    }
}