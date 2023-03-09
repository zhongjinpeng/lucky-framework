package io.lucky.authorization.server.grant.verification.code;

import io.lucky.authorization.server.constants.AuthorizationGrantTypeConstants;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Map;

/**
 * 手机验证码授权模式
 */
public class VerificationCodeTokenGrant extends AbstractTokenGranter {
    private final AuthenticationManager authenticationManager;

    public VerificationCodeTokenGrant(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, AuthenticationManager authenticationManager) {
        super(tokenServices, clientDetailsService, requestFactory, AuthorizationGrantTypeConstants.VERIFICATION_CODE_GRANT_TYPE);
        this.authenticationManager = authenticationManager;
    }


    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = tokenRequest.getRequestParameters();
        String verificationCode = parameters.get("verification_code");
        String sender = parameters.get("sender");
        if (verificationCode == null || sender == null) {
            throw new InvalidRequestException("sender and verification_code must be supplied.");
        }

        Authentication userAuth = new VerificationCodeAuthenticationToken(sender, verificationCode);
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        try {
            userAuth = authenticationManager.authenticate(userAuth);
        }
        catch (AccountStatusException ase) {
            throw new InvalidGrantException(ase.getMessage());
        }
        catch (BadCredentialsException e) {
            throw new InvalidGrantException(e.getMessage());
        }
        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate user: " + sender);
        }

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

}
