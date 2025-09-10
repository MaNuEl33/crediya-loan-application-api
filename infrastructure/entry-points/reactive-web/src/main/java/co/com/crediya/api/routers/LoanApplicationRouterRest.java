package co.com.crediya.api.routers;

import co.com.crediya.api.config.LoanApplicationPath;
import co.com.crediya.api.handlers.ApproveOrRejectLoanApplicationHandler;
import co.com.crediya.api.handlers.GlobalErrorHandler;
import co.com.crediya.api.handlers.ManualValidationApplicationReportHandler;
import co.com.crediya.api.handlers.RegisterLoanApplicationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.PATCH;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class LoanApplicationRouterRest {

    private final RegisterLoanApplicationHandler registerLoanApplicationHandler;
    private final ManualValidationApplicationReportHandler manualValidationApplicationReportHandler;
    private final ApproveOrRejectLoanApplicationHandler approveOrRejectLoanApplicationHandler;
    private final LoanApplicationPath loanApplicationPath;

    @Bean
    public RouterFunction<ServerResponse> loanApplicationRoutes() {
        return route(POST(this.loanApplicationPath.getRegister()),
                        this.registerLoanApplicationHandler::listenRegisterLoanApplication)
                .andRoute(POST(this.loanApplicationPath.getManualValidationApplicationReport()),
                        this.manualValidationApplicationReportHandler::listenManualValidationApplicationReport)
                .andRoute(PATCH(this.loanApplicationPath.getApproveOrRejectLoanApplication()),
                        this.approveOrRejectLoanApplicationHandler::listenApproveOrRejectLoanApplication)
                .filter(GlobalErrorHandler.errorHandler());
    }
}
