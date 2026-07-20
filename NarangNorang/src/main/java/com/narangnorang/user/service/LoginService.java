package com.narangnorang.user.service;

import com.narangnorang.user.dto.LoginRequestDto;
import com.narangnorang.user.dto.LoginResultDto;

public interface LoginService {

    LoginResultDto login(LoginRequestDto loginRequestDto);
}