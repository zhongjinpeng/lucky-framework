package io.lucky.authorization.server.config;

import io.lucky.authorization.server.grant.verification.code.VerificationCodeAuthenticationProvider;
import io.lucky.authorization.server.service.AuthorizationUserService;
import io.lucky.authorization.server.service.AuthorizationVerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置
 */
@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthorizationVerificationCodeService authorizationVerificationCodeService;

    @Autowired
    private AuthorizationUserService authorizationUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(authorizationUserService)
                .passwordEncoder(passwordEncoder)
                .and()
                .authenticationProvider(new VerificationCodeAuthenticationProvider(authorizationVerificationCodeService, authorizationUserService));
//                .authenticationProvider(new UsernamePasswordAuthenticationProvider(authorizationUserService,passwordEncoder));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login/**", "/oauth/authorize").permitAll() // 公开访问
                .anyRequest().authenticated() // 需要认证
                .and().formLogin().loginPage("/login") // 未认证跳转到登录页面
                .and().csrf().disable(); // 关闭csrf
    }
}
