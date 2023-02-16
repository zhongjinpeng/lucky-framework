package io.lucky.authorization.server.service;

import io.lucky.security.model.LuckyUser;

public interface AuthorizationUserService {

    LuckyUser queryUserByUsername(String username);

    LuckyUser queryUserByPhone(String phone);

}
