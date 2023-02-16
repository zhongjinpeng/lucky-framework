package io.lucky.authorization.server.config.grant.sms;

import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Map;

/**
 * 手机验证码授权模式
 */
public class SmsTokenGrant extends AbstractTokenGranter {
    private static final String GRANT_TYPE = "sms";

    private final AuthenticationManager authenticationManager;

    public SmsTokenGrant(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType, AuthenticationManager authenticationManager) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.authenticationManager = authenticationManager;
    }


    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = tokenRequest.getRequestParameters();
        String verificationCode = parameters.get("verification.code");
        String phone = parameters.get("phone");
        if (verificationCode == null || phone == null) {
            throw new InvalidRequestException("Phone and verificationCode must be supplied.");
        }

        Authentication userAuth = new SmsAuthenticationToken(phone, verificationCode);
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
            throw new InvalidGrantException("Could not authenticate user: " + phone);
        }

        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }

}
