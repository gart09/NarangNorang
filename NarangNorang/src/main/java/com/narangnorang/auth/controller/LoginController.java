package com.narangnorang.auth.controller;

import com.narangnorang.auth.dto.LoginRequestDto;
import com.narangnorang.auth.dto.LoginResultDto;
import com.narangnorang.auth.service.LoginService;
import com.narangnorang.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<LoginResultDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return loginService.login(loginRequestDto);
    }

    @PostMapping("/checkRefreshToken")
    public ApiResponse<LoginResultDto> checkRefreshToken(String refreshToken){
        return loginService.checkRefreshToken(refreshToken);
    }
}
