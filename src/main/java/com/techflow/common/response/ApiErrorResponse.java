package com.techflow.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private boolean success;
    private ErrorDetails error;

    public static ApiErrorResponse of(String code, String message) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(new ErrorDetails(code, message))
                .build();
    }

    public static ApiErrorResponse of(String code, String message, Map<String, String> fields) {
        return ApiErrorResponse.builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .fields(fields)
                        .build())
                .build();
    }
}