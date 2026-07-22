package com.narangnorang.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private String result;
    private String token;
    private String refreshToken;
}