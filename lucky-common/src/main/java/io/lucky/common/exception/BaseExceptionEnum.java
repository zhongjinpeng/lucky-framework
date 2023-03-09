package io.lucky.common.exception;

public enum BaseExceptionEnum {

    MISSING_PARAMETERS_EXCEPTION("1000","missing parameters"),
    ;

    private String code;

    private String errorMessage;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    BaseExceptionEnum(String code, String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }
}
