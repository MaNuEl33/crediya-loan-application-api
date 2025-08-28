package co.com.crediya.api.exceptions;

import jakarta.validation.ConstraintViolation;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ValidationException extends RuntimeException {
        public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
            super(violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(" ")));
    }
}
