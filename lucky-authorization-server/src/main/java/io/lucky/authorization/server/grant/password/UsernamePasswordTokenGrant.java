//package io.lucky.authorization.server.grant.password;
//
//import io.lucky.authorization.server.constants.AuthorizationGrantTypeConstants;
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
// * 用户名密码验证码授权模式
// */
//public class UsernamePasswordTokenGrant extends AbstractTokenGranter {
//    private final AuthenticationManager authenticationManager;
//
//    public UsernamePasswordTokenGrant(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, AuthenticationManager authenticationManager) {
//        super(tokenServices, clientDetailsService, requestFactory, AuthorizationGrantTypeConstants.PASSWORD_GRANT_TYPE);
//        this.authenticationManager = authenticationManager;
//    }
//
//
//    @Override
//    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
//        Map<String, String> parameters = tokenRequest.getRequestParameters();
//        String password = parameters.get("password");
//        String username = parameters.get("username");
//        if (username == null || password == null) {
//            throw new InvalidRequestException("username and verificationCode must be supplied.");
//        }
//
//        Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
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
//            throw new InvalidGrantException("Could not authenticate user: " + username);
//        }
//
//        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
//        return new OAuth2Authentication(storedOAuth2Request, userAuth);
//    }
//
//}
