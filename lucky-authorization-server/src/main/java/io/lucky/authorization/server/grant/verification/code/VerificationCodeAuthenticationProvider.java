package io.lucky.authorization.server.grant.verification.code;

import io.lucky.authorization.server.exception.AuthorizationServerException;
import io.lucky.authorization.server.exception.AuthorizationServerExceptionEnum;
import io.lucky.authorization.server.service.AuthorizationUserService;
import io.lucky.authorization.server.service.AuthorizationVerificationCodeService;
import io.lucky.security.model.LuckyUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class VerificationCodeAuthenticationProvider implements AuthenticationProvider {

    private AuthorizationVerificationCodeService authorizationVerificationCodeService;

    private AuthorizationUserService authorizationUserService;

    public VerificationCodeAuthenticationProvider(AuthorizationVerificationCodeService authorizationVerificationCodeService, AuthorizationUserService authorizationUserService) {
        this.authorizationVerificationCodeService = authorizationVerificationCodeService;
        this.authorizationUserService = authorizationUserService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        VerificationCodeAuthenticationToken verificationCodeAuthenticationToken = (VerificationCodeAuthenticationToken) authentication;
        Object principal = verificationCodeAuthenticationToken.getPrincipal();
        if (principal == null) {
            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.VERIFICATION_CODE_AUTHORIZATION_SENDER_IS_REQUIRED);
        }
        Object credentials = verificationCodeAuthenticationToken.getCredentials();
        if (credentials == null) {
            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.VERIFICATION_CODE_AUTHORIZATION_VERIFICATION_CODE_REQUIRED);
        }
        Boolean result = authorizationVerificationCodeService.checkAuthorizationVerificationCode(String.valueOf(principal), String.valueOf(credentials));
        if(!result){
            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.VERIFICATION_CODE_AUTHORIZATION_VERIFICATION_CODE_ERROR);
        }
        LuckyUser luckyUser = authorizationUserService.queryUserByPhone(String.valueOf(principal));
        return new VerificationCodeAuthenticationToken(principal,luckyUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (VerificationCodeAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
