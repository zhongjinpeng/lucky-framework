package io.lucky.authorization.server.service;

public interface AuthorizationVerificationCodeService {

    Boolean sendAuthorizationVerificationCode(String sender);

    Boolean checkAuthorizationVerificationCode(String sender,String verificationCode);

}
