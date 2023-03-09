package io.lucky.common.model.response;

public class LuckyBaseResponse<T> {

    private String status;

    private String message;

    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> LuckyBaseResponse<T> success() {
        return success(null);
    }

    public static <T> LuckyBaseResponse<T> success(T data) {
        LuckyBaseResponse luckyBaseResponse = new LuckyBaseResponse<>();
        luckyBaseResponse.setData(data);
        luckyBaseResponse.setStatus(ResponseStatusEnum.HTTP_STATUS_200.getResponseCode());
        luckyBaseResponse.setMessage(ResponseStatusEnum.HTTP_STATUS_200.getDescription());
        return luckyBaseResponse;
    }

    public static <T> LuckyBaseResponse<T> fail(String message) {
        return fail(null, message);
    }

    public static <T> LuckyBaseResponse<T> fail(T data, String message) {
        LuckyBaseResponse luckyBaseResponse = new LuckyBaseResponse<>();
        luckyBaseResponse.setData(data);
        luckyBaseResponse.setStatus(ResponseStatusEnum.FAIL.getResponseCode());
        luckyBaseResponse.setMessage(message);
        return luckyBaseResponse;
    }


    private LuckyBaseResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private LuckyBaseResponse() {
    }
}
