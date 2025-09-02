package co.com.crediya.api.security;

import co.com.crediya.api.exceptions.TokenInvalidException;
import co.com.crediya.model.user.gateways.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final TokenProvider tokenProvider;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        final var token = authentication.getCredentials().toString();

        if (this.tokenProvider.validateToken(token)) {
            final var principal = this.tokenProvider.getEmailFromToken(token);
            final var role =  this.tokenProvider.getRoleFromToken(token);
            final var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            return Mono.just(new UsernamePasswordAuthenticationToken(principal, token, authorities));
        }

        return Mono.error(new TokenInvalidException("Invalid token."));
    }
}
