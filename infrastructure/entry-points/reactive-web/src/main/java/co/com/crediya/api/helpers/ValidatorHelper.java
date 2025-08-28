package co.com.crediya.api.helpers;

import co.com.crediya.api.exceptions.ValidationException;
import jakarta.validation.Validator;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

@UtilityClass
public class ValidatorHelper {

    public static <T> Mono<T> validateObject(Mono<T> mono, Validator validator) {
        return mono.flatMap(o -> {
           final var violations =  validator.validate(o);

           if (!violations.isEmpty()) {
               return Mono.error(new ValidationException(violations));
           }

           return Mono.just(o);
        });
    }
}
