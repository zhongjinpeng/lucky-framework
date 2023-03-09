//package io.lucky.authorization.server.grant.wechat;
//
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.authentication.AccountStatusException;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
//import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
//import org.springframework.security.oauth2.provider.*;
//import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
//import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
//
//import java.util.Map;
//
///**
// * 微信登录授权模式
// */
//public class WechatTokenGrant extends AbstractTokenGranter {
//    private static final String GRANT_TYPE = "wechat";
//
//    private final AuthenticationManager authenticationManager;
//
//    public WechatTokenGrant(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType, AuthenticationManager authenticationManager) {
//        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
//        this.authenticationManager = authenticationManager;
//    }
//
//
//    @Override
//    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
//        Map<String, String> parameters = tokenRequest.getRequestParameters();
//        String verificationCode = parameters.get("verification.code");
//        String phone = parameters.get("phone");
//        if (verificationCode == null || phone == null) {
//            throw new InvalidRequestException("Phone and verificationCode must be supplied.");
//        }
//
//        Authentication userAuth = new WechatAuthenticationToken(phone, verificationCode);
//        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
//        try {
//            userAuth = authenticationManager.authenticate(userAuth);
//        }
//        catch (AccountStatusException ase) {
//            throw new InvalidGrantException(ase.getMessage());
//        }
//        catch (BadCredentialsException e) {
//            throw new InvalidGrantException(e.getMessage());
//        }
//        if (userAuth == null || !userAuth.isAuthenticated()) {
//            throw new InvalidGrantException("Could not authenticate user: " + phone);
//        }
//
//        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
//        return new OAuth2Authentication(storedOAuth2Request, userAuth);
//    }
//
//}
