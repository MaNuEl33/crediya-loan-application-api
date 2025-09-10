package co.com.crediya.sqs.sender;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplication.gateways.LoanApplicationNotifier;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import co.com.crediya.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoanApplicationSQSNotifier implements LoanApplicationNotifier {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    @Override
    public Mono<Void> notifyProcessedLoanApplication(LoanApplication loanApplication, LoanApplicationApprovalState approvalState) {
        log.info("Sending processed loan application notification to SQS");

        final var messageRequest = this.buildMessageRequest(loanApplication, approvalState);

        return Mono.fromFuture(this.client.sendMessage(messageRequest))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .doOnSuccess(r -> log.info("Processed loan application notification sent to SQS successfully."))
                .doOnError(err ->  log.error("Error sending processed loan application notification to SQS", err))
                .then();
    }

    private SendMessageRequest buildMessageRequest(LoanApplication loanApplication, LoanApplicationApprovalState approvalState) {
        final var message = new JSONObject();
        message.put("id", loanApplication.getId());
        message.put("email", loanApplication.getEmail());
        message.put("amount", loanApplication.getAmount().toPlainString());
        message.put("term", loanApplication.getTerm());
        message.put("status", approvalState.name());

        return SendMessageRequest.builder()
                .queueUrl(this.properties.queueUrl())
                .messageBody(message.toString())
                .build();
    }
}