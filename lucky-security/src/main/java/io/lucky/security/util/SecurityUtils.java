package io.lucky.security.util;

import io.lucky.security.model.LuckyUser;
import io.lucky.utils.GsonUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class SecurityUtils {

    public static LuckyUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(Objects.isNull(authentication.getPrincipal())){
            return null;
        }
        LuckyUser luckyUser = GsonUtil.GsonToBean(GsonUtil.GsonToString(authentication.getPrincipal()), LuckyUser.class);
        return luckyUser;
    }
}
