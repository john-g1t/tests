package com.github.john_g1t.presentation.web.dto;

public class JsonResponse<T> {
    private boolean success;
    private T data;
    private String error;

    public JsonResponse(boolean success, T data, String error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> JsonResponse<T> success(T data) {
        return new JsonResponse<>(true, data, null);
    }

    public static <T> JsonResponse<T> error(String message) {
        return new JsonResponse<>(false, null, message);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
