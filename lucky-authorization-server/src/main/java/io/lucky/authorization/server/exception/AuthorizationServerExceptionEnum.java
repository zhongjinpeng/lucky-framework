package io.lucky.authorization.server.exception;

public enum AuthorizationServerExceptionEnum {

    VERIFICATION_CODE_AUTHORIZATION_SENDER_IS_REQUIRED("1001", "验证码授权发送人不能为空!"),
    VERIFICATION_CODE_AUTHORIZATION_VERIFICATION_CODE_REQUIRED("1002", "验证码授权验证码不能为空!"),
    VERIFICATION_CODE_AUTHORIZATION_VERIFICATION_CODE_EXPIRED("1003", "验证码授权验证码过期!"),
    VERIFICATION_CODE_AUTHORIZATION_VERIFICATION_CODE_ERROR("1004", "验证码授权验证码错误!"),
    PASSWORD_AUTHORIZATION_USERNAME_IS_REQUIRED("1005", "密码授权用户名不能为空!"),
    PASSWORD_AUTHORIZATION_PASSWORD_IS_REQUIRED("1006", "密码授权密码不能为空!"),

    PASSWORD_AUTHORIZATION_USER_NOT_FOUND("1007", "密码授权用户不存在!"),

    PASSWORD_AUTHORIZATION_PASSWORD_ERROR("1008", "密码授权密码错误!"),

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
