package io.lucky.authorization.server.service;

import io.lucky.security.model.LuckyUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthorizationUserService extends UserDetailsService {

    LuckyUser queryUserByUsername(String username);

    LuckyUser queryUserByPhone(String phone);

}
