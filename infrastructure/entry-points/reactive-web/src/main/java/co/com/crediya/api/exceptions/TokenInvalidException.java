package co.com.crediya.api.exceptions;

import lombok.experimental.StandardException;
import org.springframework.security.core.AuthenticationException;

@StandardException
public class TokenInvalidException extends AuthenticationException {
}
