package co.com.crediya.consumer;

import co.com.crediya.consumer.dtos.UserDto;
import co.com.crediya.consumer.mappers.UserDtoMapper;
import co.com.crediya.model.user.User;
import co.com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserRestConsumer implements UserRepository {

    private final WebClient client;
    private final UserDtoMapper dtoMapper;
    
    @Override
    public Mono<User> findByEmail(String email) {
        log.info("Searching for user by email {}", email);

        return this.client.get()
                .uri("/api/v1/usuarios", uriBuilder -> uriBuilder.queryParam("email", email).build())
                .exchangeToMono(this::handleFindByEmailResponse)
                .map(this.dtoMapper::toUserModel)
                .doOnError(err -> log.error("Error searching for user by email {}.", email, err));
    }

    private Mono<UserDto> handleFindByEmailResponse(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            log.info("User founded successfully.");
            return response.bodyToMono(UserDto.class);
        } else if (response.statusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            log.info("User not found.");
            return Mono.empty();
        } else {
            return response.createException().flatMap(Mono::error);
        }
    }
}
