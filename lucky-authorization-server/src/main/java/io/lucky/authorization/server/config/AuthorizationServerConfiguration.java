package io.lucky.authorization.server.config;

import io.lucky.authorization.server.grant.verification.code.VerificationCodeTokenGrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.approval.InMemoryApprovalStore;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.util.*;

/**
 * ?????????????????????
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationServerConfiguration.class);

    @Autowired
    private AuthorizationServeClientsPropertiesConfig authorizationServeClientsPropertiesConfig;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private UserDetailsService userDetailsService;

    /**
     * ??????????????????(Token Endpoint)???????????????
     *
     * @param security a fluent configurer for security features
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 1.???????????????Token???????????????
        // 2.????????????????????????(???????????????Basic Auth????????????)
        // 3.??????ClientSecret??????????????????
        security.
                checkTokenAccess("permitAll()").
                allowFormAuthenticationForClients().
                passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    /**
     * ?????????????????????
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 1.?????????????????????????????????
        if (null == authorizationServeClientsPropertiesConfig.getClients() ||
                authorizationServeClientsPropertiesConfig.getClients().size() == 0) {
            logger.warn("AuthorizationServeClientsPropertiesConfig is null!");
            return;
        }
        ClientDetailsServiceBuilder builder = clients.inMemory();
        ClientDetailsServiceBuilder.ClientBuilder clientBuilder = null;
        for (AuthorizationServeClientsPropertiesConfig.AuthorizationServeClientConfig authorizationServeClientConfig : authorizationServeClientsPropertiesConfig.getClients()) {
            logger.debug(authorizationServeClientConfig.toString());

            ArrayList<String> authorizedGrantTypes = new ArrayList<String>();
            Collections.addAll(authorizedGrantTypes,authorizationServeClientConfig.getAuthorizedGrantTypes());
            if(authorizationServeClientsPropertiesConfig.getRefreshToken()){
                authorizedGrantTypes.add("refresh_token");
            }

            clientBuilder = builder.
                    withClient(authorizationServeClientConfig.getClientId()).
                    resourceIds(authorizationServeClientConfig.getResourceIds()).
                    secret(authorizationServeClientConfig.getClientSecret()).
                    scopes(authorizationServeClientConfig.getScope()).
                    authorizedGrantTypes(authorizedGrantTypes.toArray(new String[authorizedGrantTypes.size()])).
                    redirectUris(authorizationServeClientConfig.getRegisteredRedirectUris()).
                    accessTokenValiditySeconds(authorizationServeClientsPropertiesConfig.getAccessTokenValiditySeconds()).
                    refreshTokenValiditySeconds(authorizationServeClientsPropertiesConfig.getRefreshTokenValiditySeconds()).
                    autoApprove(authorizationServeClientConfig.getAutoApprove()).
                    additionalInformation(authorizationServeClientConfig.getAdditionalInformation());
        }
        builder = clientBuilder.and();
    }

    /**
     * ?????????????????????authorization??????????????????token?????????????????????????????????(token services)
     *
     * @param endpoints the endpoints configurer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
       switch (authorizationServeClientsPropertiesConfig.getTokenType()){
           case "jwt" :
               TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
               tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));
               endpoints.tokenStore(jwtTokenStore()).accessTokenConverter(jwtAccessTokenConverter()).tokenEnhancer(tokenEnhancerChain);
               break;
           case "redis" :
               endpoints.tokenStore(redisTokenStore());
               break;
//           case "jdbc" :
//               endpoints.tokenStore(jdbcTokenStore());
//               break;
           default:
               // TODO  ??????UnKnowTokenTypeException
       }
        endpoints.approvalStore(approvalStore())
                .authorizationCodeServices(authorizationCodeServices())
                .tokenGranter(tokenGranter(endpoints))
                .authenticationManager(authenticationManager);
       if(Objects.nonNull(userDetailsService)){
           endpoints.userDetailsService(userDetailsService);
       }
    }

    /**
     * ??????????????????
     * @param endpoints
     * @return
     */
    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> tokenGranterList = new ArrayList<>(Arrays.asList(endpoints.getTokenGranter()));
        // ???????????????TokenGranter
        tokenGranterList.add(new VerificationCodeTokenGrant(authorizationServerTokenServices(),clientDetailsService,endpoints.getOAuth2RequestFactory(),authenticationManager));
//        tokenGranterList.add(new UsernamePasswordTokenGrant(authorizationServerTokenServices(),clientDetailsService,endpoints.getOAuth2RequestFactory(),authenticationManager));
        return new CompositeTokenGranter(tokenGranterList);
    }

    @Bean("authorizationServerTokenServices")
    public AuthorizationServerTokenServices authorizationServerTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
//        tokenServices.setAccessTokenValiditySeconds(authorizationServeClientsPropertiesConfig.getAccessTokenValiditySeconds());
//        tokenServices.setRefreshTokenValiditySeconds(authorizationServeClientsPropertiesConfig.getRefreshTokenValiditySeconds());
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setAuthenticationManager(authenticationManager);
        tokenServices.setSupportRefreshToken(authorizationServeClientsPropertiesConfig.getRefreshToken());
        switch (authorizationServeClientsPropertiesConfig.getTokenType()){
            case "jwt" :
                tokenServices.setTokenStore(jwtTokenStore());
                TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
                tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));
                tokenServices.setTokenEnhancer(tokenEnhancerChain);
                break;
            case "redis" :
                tokenServices.setTokenStore(redisTokenStore());
                break;
//            case "jdbc" :
//                tokenServices.setTokenStore(jdbcTokenStore());
//                break;
            default:
                // TODO  ??????UnKnowTokenTypeException
        }
        return tokenServices;
    }

    /**
     * ??????Token???????????????
     * @return
     */
    @Bean("authorizationServerJwtTokenStore")
    @ConditionalOnProperty(prefix = "authorization-server", name = "token-type",havingValue = "jwt")
    public TokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean("authorizationServerJwtAccessTokenConverter")
    @ConditionalOnProperty(prefix = "authorization-server", name = "token-type",havingValue = "jwt")
    protected JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource("public.cert");
        String publicKey = null;
        try {
            publicKey = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        converter.setVerifierKey(publicKey);
        return converter;
    }

    @Bean("authorizationServerRedisTokenStore")
    @ConditionalOnProperty(prefix = "authorization-server", name = "token-type",havingValue = "redis")
    protected TokenStore redisTokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

//    @Bean("authorizationServerJdbcTokenStore")
//    @ConditionalOnProperty(prefix = "authorization-server", name = "token-type",havingValue = "jdbc")
//    protected TokenStore jdbcTokenStore() {
//        return new JdbcTokenStore(dataSource);
//    }

    /**
     * ???????????????????????????
     * @return
     */
    @Bean("authorizationServerAuthorizationCodeServices")
    public AuthorizationCodeServices authorizationCodeServices() {
        return new InMemoryAuthorizationCodeServices();
    }

    /**
     * ????????????????????????
     * @return
     */
    @Bean("authorizationServerInMemoryApprovalStore")
    public InMemoryApprovalStore approvalStore() {
        return new InMemoryApprovalStore();
    }

    /**
     * ?????????Token?????????,???????????????????????????Token
     * @return
     */
    @Bean("authorizationServerTokenEnhancer")
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }



}
