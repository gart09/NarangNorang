package com.narangnorang.auth.service;

import com.narangnorang.auth.dto.LoginRequestDto;
import com.narangnorang.auth.dto.LoginResultDto;
import com.narangnorang.common.ApiResponse;

public interface LoginService {

    ApiResponse<LoginResultDto> login(LoginRequestDto loginRequestDto);
    ApiResponse<LoginResultDto> checkRefreshToken(String refreshToken);
}