package co.com.crediya.consumer;

import co.com.crediya.consumer.dtos.UserDto;
import co.com.crediya.consumer.mappers.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserRestConsumer implements UserRepository {

    private final WebClient client;
    private final UserDtoMapper dtoMapper;

    @Override
    @CircuitBreaker(name = "authServiceFindByEmail")
    public Mono<User> findByEmail(String email) {
        log.info("Searching for user with email {}", email);

        return Mono.deferContextual(ctx -> this.client.get()
                .uri("/api/v1/usuarios",
                        uriBuilder -> uriBuilder.queryParam("email", email).build())
                .headers(headers -> headers.setBearerAuth(ctx.get("token")))
                .retrieve()
                .bodyToFlux(UserDto.class)
                .single()
                .map(this.dtoMapper::toUserModel)
                .doOnSuccess(u -> log.info("User founded successfully."))
                .doOnError(err -> log.error("Error searching user with email {}", email, err))
        );
    }
}
