package io.lucky.authorization.server.config.grant.sms;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.Map;

/**
 * 手机验证码授权模式
 */
public class SmsTokenGrant extends AbstractTokenGranter {
    private static final String GRANT_TYPE = "sms";

    protected SmsTokenGrant(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = tokenRequest.getRequestParameters();
        String verificationCode = parameters.get("verification.code");
        String phone = parameters.get("phone");
        if (verificationCode == null || phone == null) {
            throw new InvalidRequestException("An authorization code must be supplied.");
        }
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        Authentication userAuth = new SmsAuthenticationToken(phone, verificationCode);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);

    }

}
