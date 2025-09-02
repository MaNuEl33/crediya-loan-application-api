package co.com.crediya.jwttokenprovider.config;

import co.com.crediya.jwttokenprovider.JwtTokenProviderAdapter;
import co.com.crediya.model.user.gateways.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class JwtConfig {

    @Bean
    public TokenProvider tokenProvider(Environment environment) {
        final var secret = environment.getProperty("jwt.secret");

        return new JwtTokenProviderAdapter(secret);
    }
}
