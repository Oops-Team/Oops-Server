package com.oops.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DefaultResponse<T> {

    private int status;
    private String message;
    private T data;

    public static <T> DefaultResponse<T> from(final int status, final String message) {
        return from(status, message, null);
    }

    public static <T> DefaultResponse<T> from(final int status, final String message, final T data) {
        return DefaultResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}
