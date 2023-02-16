package io.lucky.authorization.server.exception;

public enum AuthorizationServerExceptionEnum {

    SMS_AUTHORIZATION_PHONE_IS_REQUIRED("1001", "sms授权手机号不能为空!"),
    SMS_AUTHORIZATION_VERIFICATION_CODE_REQUIRED("1002", "sms授权验证码不能为空!"),
    SMS_AUTHORIZATION_VERIFICATION_CODE_EXPIRED("1003", "sms授权验证码过期!"),
    SMS_AUTHORIZATION_VERIFICATION_CODE_ERROR("1004", "sms授权验证码错误!"),
    ;

    private String code;
    private String message;

    AuthorizationServerExceptionEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
