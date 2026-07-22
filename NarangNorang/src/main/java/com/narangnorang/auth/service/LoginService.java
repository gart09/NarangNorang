package com.narangnorang.auth.service;

import com.narangnorang.auth.dto.request.LoginRequestDto;
import com.narangnorang.auth.dto.response.LoginResponseDto;
import com.narangnorang.common.ApiResponse;

public interface LoginService {

    ApiResponse<LoginResponseDto> login(LoginRequestDto loginRequestDto);
    ApiResponse<LoginResponseDto> checkRefreshToken(String refreshToken);
}