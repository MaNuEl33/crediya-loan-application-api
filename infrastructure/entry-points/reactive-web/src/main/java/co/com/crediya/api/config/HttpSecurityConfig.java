package co.com.crediya.api.config;

import co.com.crediya.api.dtos.ErrorResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Log4j2
public class HttpSecurityConfig {
    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerAuthenticationConverter serverAuthenticationConverter;
    private final LoanApplicationPath loanApplicationPath;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        final var jwtAuthFilter = this.buildAuthenticationWebFilter();

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(this.loanApplicationPath.getRegister()).hasRole("APPLICANT")
                        .pathMatchers(this.loanApplicationPath.getManualValidationApplicationReport()).hasRole("ADVISOR")
                        .pathMatchers(this.loanApplicationPath.getApproveOrRejectLoanApplication()).hasRole("ADVISOR")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(this::handleAccessDeniedError)
                )
                .build();
    }

    private AuthenticationWebFilter buildAuthenticationWebFilter() {
        final var jwtAuthFilter = new AuthenticationWebFilter(this.authenticationManager);

        jwtAuthFilter.setServerAuthenticationConverter(this.serverAuthenticationConverter);
        jwtAuthFilter.setAuthenticationFailureHandler((wex, e) ->
                this.handleAuthenticationFailureError(wex.getExchange(), e));

        return jwtAuthFilter;
    }

    private Mono<Void> handleAuthenticationFailureError(ServerWebExchange exchange, AuthenticationException e) {
        try {
            log.error("Authentication Failure Error.", e);
            final var errorResponse = new ErrorResponseDto(HttpStatus.UNAUTHORIZED.value(), "Invalid token.");
            final var bytes = this.objectMapper.writeValueAsBytes(errorResponse);

            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (JsonProcessingException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    private Mono<Void> handleAccessDeniedError(ServerWebExchange exchange, AccessDeniedException e) {
        try {
            log.error("Access Denied Error.", e);
            final var errorResponse = new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), "You do not have sufficient permissions.");
            final var bytes = this.objectMapper.writeValueAsBytes(errorResponse);

            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        } catch (JsonProcessingException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }
}
