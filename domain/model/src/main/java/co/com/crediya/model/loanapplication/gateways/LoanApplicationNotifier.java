package co.com.crediya.model.loanapplication.gateways;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import reactor.core.publisher.Mono;

public interface LoanApplicationNotifier {
    Mono<Void> notifyProcessedLoanApplication(LoanApplication loanApplication, LoanApplicationApprovalState approvalState);
}
