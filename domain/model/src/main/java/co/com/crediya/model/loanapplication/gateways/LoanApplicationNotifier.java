package co.com.crediya.model.loanapplication.gateways;

import co.com.crediya.model.loanapplication.LoanApplication;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationApprovalState;
import co.com.crediya.model.loanapplicationstate.enums.LoanApplicationCodeState;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface LoanApplicationNotifier {
    Mono<Void> notifyProcessedLoanApplication(LoanApplication loanApplication, LoanApplicationApprovalState approvalState);
    Mono<Void> notifyDebtCapacityCalculation(List<LoanApplication> activeLoans, BigDecimal baseSalary, LoanApplication loanApplication);
    Mono<Void> notifyAutomaticValidationLoanApplicationState(LoanApplication loanApplication, LoanApplicationCodeState codeState);
    Mono<Void> notifyAcceptedLoanApplication(LoanApplication loanApplication);
}
