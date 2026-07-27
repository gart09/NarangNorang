package com.narangnorang.auth.service;

import com.narangnorang.auth.dto.request.LoginRequestDto;
import com.narangnorang.auth.dto.response.LoginResponseDto;
import com.narangnorang.common.ApiResponse;

public interface LoginService {

    LoginResponseDto login(LoginRequestDto loginRequestDto);
    LoginResponseDto checkRefreshToken(String refreshToken);
    void logout(Long userId);
}