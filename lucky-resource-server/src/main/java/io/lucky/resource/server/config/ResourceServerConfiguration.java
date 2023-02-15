package io.lucky.resource.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

/**
 * 资源服务器
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Autowired
    private ResourcePropertiesConfig resourcePropertiesConfig;

    /**
     * 声明资源服务器的ID，声明了资源服务器的TokenStore类型(默认JWT,支持redis、jdbc等)
     *
     * @param resources
     * @throws Exception
     */
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId(resourcePropertiesConfig.getResourceId()).tokenStore(new JwtTokenStore(jwtAccessTokenConverter()));
    }

    @Bean
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

    /**
     * 配置资源访问权限
     *
     * @param http
     * @throws Exception
     */
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(null != resourcePropertiesConfig.getWhiteList() ? resourcePropertiesConfig.getWhiteList().stream().toArray(String[]::new) : null).permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }

}
