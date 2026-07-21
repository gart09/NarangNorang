package com.narangnorang.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResultDto {

    private String result;
    private String token;
    private String refreshToken;
}