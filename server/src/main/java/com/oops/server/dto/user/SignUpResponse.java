package com.oops.server.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResponse {

    private String message;
    private int status;
    private String token;
    private Long userId;
}
