package co.com.crediya.api.routers;

import co.com.crediya.api.config.LoanApplicationPath;
import co.com.crediya.api.handlers.GlobalErrorHandler;
import co.com.crediya.api.handlers.LoanApplicationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class LoanApplicationRouterRest {

    private final LoanApplicationHandler loanApplicationHandler;
    private final LoanApplicationPath loanApplicationPath;

    @Bean
    public RouterFunction<ServerResponse> loanApplicationRoutes() {
        return route(POST(this.loanApplicationPath.getRegister()), this.loanApplicationHandler::listenRegisterLoanApplication)
                .filter(GlobalErrorHandler.errorHandler());
    }
}
