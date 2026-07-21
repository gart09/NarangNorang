package com.narangnorang.auth.service;

import com.narangnorang.auth.dto.LoginRequestDto;
import com.narangnorang.auth.dto.LoginResultDto;

public interface LoginService {

    LoginResultDto login(LoginRequestDto loginRequestDto);
    LoginResultDto checkRefreshToken(String refreshToken);
}