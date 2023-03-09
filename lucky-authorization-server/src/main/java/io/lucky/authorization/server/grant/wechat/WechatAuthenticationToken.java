//package io.lucky.authorization.server.grant.wechat;
//
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.util.Assert;
//
//import java.util.Collection;
//
//public class WechatAuthenticationToken extends AbstractAuthenticationToken {
//
//    private final Object principal;
//    private Object credentials;
//
//    public WechatAuthenticationToken(Object principal, Object credentials) {
//        super((Collection)null);
//        this.principal = principal;
//        this.credentials = credentials;
//        this.setAuthenticated(false);
//    }
//
//    public WechatAuthenticationToken(Object principal , Collection<? extends GrantedAuthority> authorities) {
//        super(authorities);
//        this.principal = principal;
//        super.setAuthenticated(true);
//    }
//
//    @Override
//    public Object getCredentials() {
//        return this.credentials;
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return this.principal;
//    }
//
//    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
//        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
//        super.setAuthenticated(false);
//    }
//
//}
