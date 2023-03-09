//package io.lucky.authorization.server.grant.password;
//
//import io.lucky.authorization.server.exception.AuthorizationServerException;
//import io.lucky.authorization.server.exception.AuthorizationServerExceptionEnum;
//import io.lucky.authorization.server.service.AuthorizationUserService;
//import io.lucky.authorization.server.service.AuthorizationVerificationCodeService;
//import io.lucky.security.model.LuckyUser;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Objects;
//
//public class UsernamePasswordAuthenticationProvider implements AuthenticationProvider {
//    private AuthorizationUserService authorizationUserService;
//
//    private PasswordEncoder passwordEncoder;
//
//    public UsernamePasswordAuthenticationProvider(AuthorizationUserService authorizationUserService, PasswordEncoder passwordEncoder) {
//        this.authorizationUserService = authorizationUserService;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//
//        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
//        Object principal = usernamePasswordAuthenticationToken.getPrincipal();
//        if (principal == null) {
//            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.PASSWORD_AUTHORIZATION_USERNAME_IS_REQUIRED);
//        }
//        Object credentials = usernamePasswordAuthenticationToken.getCredentials();
//        if (credentials == null) {
//            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.PASSWORD_AUTHORIZATION_PASSWORD_IS_REQUIRED);
//        }
//
//        LuckyUser luckyUser = authorizationUserService.queryUserByUsername(String.valueOf(principal));
//        if(Objects.isNull(luckyUser)){
//            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.PASSWORD_AUTHORIZATION_USER_NOT_FOUND);
//        }
//        if(!passwordEncoder.matches(luckyUser.getPassword(),String.valueOf(credentials))){
//            throw AuthorizationServerException.authorizationServerException(AuthorizationServerExceptionEnum.PASSWORD_AUTHORIZATION_PASSWORD_ERROR);
//        }
//        return new UsernamePasswordAuthenticationToken(principal,luckyUser.getAuthorities());
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
//    }
//
//}
