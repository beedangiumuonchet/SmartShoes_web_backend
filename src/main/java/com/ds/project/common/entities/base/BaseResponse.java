package com.ds.project.common.entities.base;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<TData> {

    private Optional<TData> result = Optional.empty();
    private Optional<String> message = Optional.empty();

    public BaseResponse(String message) {
        this.message = Optional.of(message);
    }

    public BaseResponse(TData result) {
        this.result = Optional.of(result);
    }

    // --- Factory methods ---
    public static <T> BaseResponse<T> of(T data) {
        return new BaseResponse<>(data);
    }

    public static <T> BaseResponse<T> of(Optional<T> data, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setResult(data);
        response.setMessage(Optional.ofNullable(message));
        return response;
    }

    public static <T> BaseResponse<T> error(String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setResult(Optional.empty());
        response.setMessage(Optional.ofNullable(message));
        return response;
    }

    // ✅ Thêm các method tiện ích cho success
    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setResult(Optional.ofNullable(data));
        response.setMessage(Optional.of("Success"));
        return response;
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setResult(Optional.ofNullable(data));
        response.setMessage(Optional.ofNullable(message));
        return response;
    }

    public static <T> BaseResponse<T> successMessage(String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setMessage(Optional.ofNullable(message));
        return response;
    }
}
