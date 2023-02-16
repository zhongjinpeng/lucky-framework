package io.lucky.authorization.server.exception;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

public class AuthorizationServerException extends AuthenticationException {

    public AuthorizationServerException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AuthorizationServerException(String msg) {
        super(msg);
    }

    public static AuthorizationServerException authorizationServerException(AuthorizationServerExceptionEnum authorizationServerExceptionEnum) {
        return new AuthorizationServerException(authorizationServerExceptionEnum.getCode());
    }

}
