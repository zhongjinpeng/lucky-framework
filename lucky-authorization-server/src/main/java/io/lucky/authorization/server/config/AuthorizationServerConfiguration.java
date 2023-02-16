package io.lucky.authorization.server.config;

import io.lucky.authorization.server.grant.sms.SmsTokenGrant;
import io.lucky.authorization.server.constants.AuthorizationServeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 授权服务器配置
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

    /**
     * 配置令牌端点(Token Endpoint)的安全约束
     *
     * @param security a fluent configurer for security features
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 1.打开了验证Token的访问权限
        // 2.可以通过表单提交(而不仅仅是Basic Auth方式提交)
        // 3.允许ClientSecret明文方式保存
        security.
                checkTokenAccess("permitAll()").
                allowFormAuthenticationForClients().
                passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    /**
     * 配置客户端信息
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 1.基于内存配置客户端信息
        if (null == authorizationServeClientsPropertiesConfig.getClients() ||
                authorizationServeClientsPropertiesConfig.getClients().size() == 0) {
            logger.warn("AuthorizationServeClientsPropertiesConfig is null!");
            return;
        }
        ClientDetailsServiceBuilder builder = clients.inMemory();
        ClientDetailsServiceBuilder.ClientBuilder clientBuilder = null;
        for (AuthorizationServeClientsPropertiesConfig.AuthorizationServeClientConfig authorizationServeClientConfig : authorizationServeClientsPropertiesConfig.getClients()) {
            logger.debug(authorizationServeClientConfig.toString());
            clientBuilder = builder.
                    withClient(authorizationServeClientConfig.getClientId()).
                    resourceIds(authorizationServeClientConfig.getResourceIds()).
                    secret(authorizationServeClientConfig.getClientSecret()).
                    scopes(authorizationServeClientConfig.getScope()).
                    authorizedGrantTypes(authorizationServeClientConfig.getAuthorizedGrantTypes()).
                    redirectUris(authorizationServeClientConfig.getRegisteredRedirectUris()).
                    accessTokenValiditySeconds(authorizationServeClientConfig.getAccessTokenValiditySeconds()).
                    refreshTokenValiditySeconds(authorizationServeClientConfig.getRefreshTokenValiditySeconds()).
                    autoApprove(authorizationServeClientConfig.getAutoApprove()).
                    additionalInformation(authorizationServeClientConfig.getAdditionalInformation());
        }
        builder = clientBuilder.and();
    }

    /**
     * 用来配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)
     *
     * @param endpoints the endpoints configurer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 1.配置我们的令牌存放方式为JWT方式，而不是内存、数据库或Redis方式
        // 2.配置JWT Token的非对称加密来进行签名
        // 3.配置一个自定义的Token增强器，把更多信息放入Token中
        // 4.配置使用JDBC数据库方式来保存用户的授权批准记录
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
                Arrays.asList(tokenEnhancer(), jwtTokenEnhancer()));
        endpoints.approvalStore(approvalStore())
                .authorizationCodeServices(authorizationCodeServices())
                .tokenServices(authorizationServerTokenServices())
                .tokenStore(tokenStore())
                .tokenGranter(tokenGranter(endpoints))
                .tokenEnhancer(tokenEnhancerChain)
                .authenticationManager(authenticationManager);
    }

    /**
     * 配置授权模式
     * @param endpoints
     * @return
     */
    private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> tokenGranterList = new ArrayList<>(Arrays.asList(endpoints.getTokenGranter()));
        // 添加自定义TokenGranter
        tokenGranterList.add(new SmsTokenGrant(authorizationServerTokenServices(),clientDetailsService,endpoints.getOAuth2RequestFactory(),"sms",authenticationManager));
        return new CompositeTokenGranter(tokenGranterList);
    }

    @Bean
    public AuthorizationServerTokenServices authorizationServerTokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setAccessTokenValiditySeconds(AuthorizationServeConstants.DEFAULT_ACCESS_TOKEN_VALIDITY_SECONDS);
        tokenServices.setRefreshTokenValiditySeconds(AuthorizationServeConstants.DEFAULT_REFRESH_TOKEN_VALIDITY_SECONDS);
        tokenServices.setClientDetailsService(clientDetailsService);
        tokenServices.setAuthenticationManager(authenticationManager);
        tokenServices.setSupportRefreshToken(false);
        tokenServices.setTokenStore(tokenStore());
        return tokenServices;
    }

    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new InMemoryAuthorizationCodeServices();
    }

    /**
     * 配置Token持久化方式
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }

    @Bean
    public InMemoryApprovalStore approvalStore() {
        return new InMemoryApprovalStore();
    }

    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }

    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
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

}
